package io.mvnpm.esbuild.deno;

import java.io.*;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mvnpm.esbuild.BundlingException;
import io.mvnpm.esbuild.model.BundleOptions;
import io.mvnpm.esbuild.model.EsBuildPlugin;

public class DenoRunner {
    private static final Logger LOG = Logger.getLogger(DenoRunner.class);
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("DenoRunner-Thread");
        return t;
    });
    private static final Map<String, String> DENO_BINARIES = Map.of(
            "macos-arm64", "darwin-arm64",
            "macos-x64", "darwin-x64",
            "linux-arm64", "linux-arm64-glibc",
            "linux-x64", "linux-x64-glibc",
            "windows-arm64", "win32-arm64",
            "windows-x64", "win32-x64");

    public static ScriptLog runDenoScript(Path workDir, Path nodeModules, String scriptContent, long timeoutSeconds)
            throws IOException {
        final Path denoBinary = getDenoBinary(nodeModules);

        final Path scriptFile = prepareScript(workDir, scriptContent);

        final Process process = startProcess(workDir, denoBinary, scriptFile);
        return waitForExit(process, timeoutSeconds);
    }

    public static Process devDenoScript(Path workDir, Path nodeModules, String scriptContent)
            throws IOException {
        final Path denoBinary = getDenoBinary(nodeModules);
        final Path scriptFile = prepareScript(workDir, scriptContent);

        return startProcess(workDir, denoBinary, scriptFile);
    }

    public static String formatScript(String template, Path workDir, BundleOptions bundleOptions) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        bundleOptions.plugins().forEach(p -> p.beforeBuild(workDir));
        final String importPluginsJs = bundleOptions.plugins().stream().map(EsBuildPlugin::importScript)
                .collect(Collectors.joining("\n"));
        final String pluginsJs = mapper
                .writeValueAsString(bundleOptions.plugins().stream().map(EsBuildPlugin::toMap).toList());
        return template.formatted(importPluginsJs, supportsColor().toString(), pluginsJs,
                bundleOptions.esBuildConfig().toJson());
    }

    private static Boolean supportsColor() {
        return System.console() != null && System.getenv().get("TERM") != null;
    }

    public static ScriptLog waitForExit(Process process, long timeoutSeconds) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        Future<ScriptLog> future = EXECUTOR.submit(() -> {
            ScriptLog log = new ScriptLog();
            try (reader) {
                String line;
                while ((line = reader.readLine()) != null && process.isAlive()) {
                    log.add(line);
                }
            } catch (IOException ignored) {
                // Reader closed due to timeout or interruption
            }
            return log;
        });

        return scriptLog(process, reader, future, true, timeoutSeconds);
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }

    public static ScriptLog waitForResult(Process process, long timeoutSeconds) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        Future<ScriptLog> future = EXECUTOR.submit(() -> {
            ScriptLog log = new ScriptLog();
            try {
                String line;
                while ((line = reader.readLine()) != null && process.isAlive()) {
                    switch (line) {
                        case "--BUILD--" -> {
                            continue;
                        }
                        case "--READY--", "--BUILD-SUCCESS--" -> {
                            return log;
                        }
                        case "--BUILD-ERROR--" -> {
                            log.logAll();
                            throw new BundlingException("EsBuild Bundling failed", log);
                        }
                    }
                    log.add(line);
                }
            } catch (IOException ignored) {
                // Reader closed due to timeout or interruption
            }
            return log;
        });

        return scriptLog(process, reader, future, false, timeoutSeconds);
    }

    private static ScriptLog scriptLog(Process process, BufferedReader reader, Future<ScriptLog> future, boolean waitForExit,
            long timeoutSeconds) {
        try {
            ScriptLog log = future.get(timeoutSeconds, TimeUnit.SECONDS);
            if (waitForExit) {
                boolean finished = process.waitFor(50, TimeUnit.MILLISECONDS);
                int exitCode = finished ? process.exitValue() : -1;
                if (exitCode != 0) {
                    log.logAll();
                    throw new BundlingException("EsBuild Bundling failed", log);
                }
            } else if (!process.isAlive()) {
                log.logAll();
                throw new RuntimeException("Bundling process exited unexpectedly");
            }

            return log;

        } catch (TimeoutException | InterruptedException e) {
            future.cancel(true);
            closeQuietly(reader);
            process.destroy();
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Bundling process was interrupted", e);
            }
            throw new BundlingException("Bundling process did not stop after " + timeoutSeconds + " seconds");

        } catch (ExecutionException e) {
            if (waitForExit) {
                closeQuietly(reader);
                process.destroy();
            }
            if (e.getCause() instanceof BundlingException) {
                throw (BundlingException) e.getCause();
            }
            throw new RuntimeException("Bundling process exited unexpectedly", e);
        }
    }

    private static Process startProcess(Path workDir, Path denoBinary, Path scriptFile) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                denoBinary.toAbsolutePath().toString(),
                "run",
                "--allow-all",
                "--node-modules-dir=manual",
                scriptFile.toAbsolutePath().toString());
        LOG.debugf("Running esbuild script ''%s'' ", workDir);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        return process;
    }

    private static Path prepareScript(Path workDir, String scriptContent) throws IOException {
        Path scriptFile = workDir.resolve("build.js");
        Files.write(scriptFile, scriptContent.getBytes());
        return scriptFile;
    }

    private static Path getDenoBinary(Path nodeModules) {
        final String classifier = determineClassifier();
        final String name = DENO_BINARIES.get(classifier);
        Path denoBinary = nodeModules
                .resolve("@deno/%s/deno%s".formatted(name, getExecutableExtension()));

        if (!Files.isRegularFile(denoBinary)) {
            throw new BundlingException("Deno binary file not found for EsBuild Java: " + denoBinary);
        }

        denoBinary.toFile().setExecutable(true);
        return denoBinary;
    }

    private static String determineClassifier() {
        final String osName = System.getProperty("os.name").toLowerCase();
        final String osArch = System.getProperty("os.arch").toLowerCase();
        String classifier = null;
        if (osName.contains("mac")) {
            if (osArch.equals("aarch64") || osArch.contains("arm")) {
                classifier = "macos-arm64";
            } else {
                classifier = "macos-x64";
            }
        } else if (osName.contains("win")) {
            if (osArch.equals("aarch64") || osArch.equals("arm64")) {
                classifier = "windows-arm64";
            } else if (osArch.contains("64")) {
                classifier = "windows-x64";
            }
        } else {
            if (osArch.equals("aarch64") || osArch.equals("arm64")) {
                classifier = "linux-arm64";
            } else if (osArch.contains("64")) {
                classifier = "linux-x64";
            }
        }
        if (classifier == null) {
            throw new BundlingException("Incompatible os: '%s' and arch: '%s' for EsBuild Java".formatted(osName, osArch));
        }
        return classifier;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static String getExecutableExtension() {
        return isWindows() ? ".exe" : "";
    }
}
