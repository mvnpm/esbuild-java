package io.mvnpm.esbuild.deno;

import java.io.*;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mvnpm.esbuild.BundleException;
import io.mvnpm.esbuild.model.BundleOptions;
import io.mvnpm.esbuild.model.EsBuildPlugin;

public class DenoRunner {

    private static final Map<String, String> DENO_BINARIES = Map.of(
            "macos-arm64", "darwin-arm64",
            "macos-x64", "darwin-x64",
            "linux-arm64", "linux-arm64-glibc",
            "linux-x64", "linux-x64-glibc",
            "windows-arm64", "win32-arm64",
            "windows-x64", "win32-x64");

    public static String runDenoScript(Path workDir, Path nodeModules, String scriptContent)
            throws IOException {
        final Path denoBinary = getDenoBinary(nodeModules);

        final Path scriptFile = prepareScript(workDir, scriptContent);

        final Process process = startProcess(workDir, denoBinary, scriptFile);
        return waitForProcess(process);
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
        return template.formatted(importPluginsJs, pluginsJs, bundleOptions.esBuildConfig().toJson());
    }

    public static String waitForResult(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null && process.isAlive()) {
            switch (line) {
                case "--BUILD--" -> {
                    continue;
                }
                case "--READY--", "--BUILD-SUCCESS--" -> {
                    return output.toString();
                }
                case "--BUILD-ERROR--" -> throw new BundleException("Error while executing Esbuild", output.toString());
            }
            output.append(line).append("\n");
        }
        throw new BundleException("Bundling exited unexpectedly.");
    }

    public static String waitForProcess(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            reader.lines().forEach(line -> output.append(line).append(System.lineSeparator()));
        }

        try {
            final boolean exited = process.waitFor(30, TimeUnit.SECONDS);
            if (!exited) {
                process.destroy();
                throw new BundleException("Bundling did not stop after 30 seconds.");
            }
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new BundleException("EsBuild Bundling failed:\n" + output);
            }
            return output.toString();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

    }

    private static Process startProcess(Path workDir, Path denoBinary, Path scriptFile) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                denoBinary.toAbsolutePath().toString(),
                "run",
                "--allow-all",
                "--node-modules-dir=manual",
                scriptFile.toAbsolutePath().toString());
        System.out.println("Running esbuild script: " + workDir);
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
            throw new BundleException("Deno binary file not found for EsBuild Java: " + denoBinary);
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
            throw new BundleException("Incompatible os: '%s' and arch: '%s' for EsBuild Java".formatted(osName, osArch));
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
