package io.mvnpm.esbuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.mvnpm.esbuild.model.EsBuildConfig;
import io.mvnpm.esbuild.model.ExecuteResult;

public class Execute {

    private static final ExecutorService EXECUTOR_STREAMER = Executors.newSingleThreadExecutor(r -> {
        final Thread t = new Thread(r, "Process stdout streamer");
        t.setDaemon(true);
        return t;
    });
    private static final ExecutorService EXECUTOR_BUILD_LISTENERS = Executors
            .newSingleThreadExecutor(r -> new Thread(r, "Build Listeners"));
    private static final Logger logger = Logger.getLogger(Execute.class.getName());

    private final Path workDir;
    private final File esBuildExec;
    private EsBuildConfig esBuildConfig;
    private String[] args;

    public Execute(Path workDir, File esBuildExec, EsBuildConfig esBuildConfig) {
        this.workDir = workDir;
        this.esBuildExec = esBuildExec;
        this.esBuildConfig = esBuildConfig;
    }

    public Execute(Path workDir, File esBuildExec, String[] args) {
        this.workDir = workDir;
        this.esBuildExec = esBuildExec;
        this.args = args;
    }

    public ExecuteResult executeAndWait() throws IOException {
        final Process process = createProcess(getCommand(), Optional.empty());
        try {
            final int exitCode = process.waitFor();
            final String content = readStream(process.getInputStream());
            final String errors = readStream(process.getErrorStream());
            if (exitCode != 0) {
                throw new BundleException(errors.isEmpty() ? "Unexpected Error during bundling" : errors, content);
            }
            return new ExecuteResult(content);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public Process execute(BuildEventListener listener) throws IOException {
        return createProcess(getCommand(), Optional.of(listener));
    }

    private String[] getCommand() {
        String[] command = args != null ? getCommand(args) : getCommand(esBuildConfig);
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "running esbuild with flags: \n > `{0}`", String.join(" ", command));
        }
        return command;
    }

    private String[] getCommand(EsBuildConfig esBuildConfig) {
        String[] params = esBuildConfig.toParams();
        return getCommand(params);
    }

    private String[] getCommand(String[] args) {
        List<String> argList = new ArrayList<>(args.length + 1);
        argList.add(esBuildExec.toString());
        argList.addAll(Arrays.asList(args));

        return argList.toArray(String[]::new);
    }

    public Process createProcess(final String[] command, final Optional<BuildEventListener> listener) throws IOException {
        Process process = new ProcessBuilder().redirectErrorStream(listener.isPresent()).directory(workDir.toFile())
                .command(command).start();
        final InputStream errorStream = process.getInputStream();
        listener.ifPresent(
                buildEventListener -> EXECUTOR_STREAMER
                        .execute(new Streamer(process::isAlive, errorStream, buildEventListener)));
        return process;
    }

    private record Streamer(BooleanSupplier isAlive, InputStream processStream,
            BuildEventListener listener) implements Runnable {

        @Override
        public void run() {
            final StringBuilder errorBuilder = new StringBuilder();
            consumeStream(isAlive, processStream, l -> {
                logger.fine(l);
                if (l.contains("build finished")) {
                    logger.fine("Build finished!");
                    final String error = errorBuilder.toString();
                    errorBuilder.setLength(0);
                    EXECUTOR_BUILD_LISTENERS.execute(() -> {
                        if (error.isEmpty()) {
                            listener.onBuild(Optional.empty());
                        } else {
                            listener.onBuild(Optional.of(new BundleException("Error during bundling", error)));
                        }
                    });
                } else if (l.contains("[ERROR]") || !errorBuilder.isEmpty()) {
                    errorBuilder.append("\n").append(l);
                }
            });
        }
    }

    private static String readStream(InputStream stream) {
        final StringBuilder s = new StringBuilder();
        consumeStream(() -> true, stream, l -> s.append(l).append("\n"));
        return s.toString();
    }

    private static void consumeStream(BooleanSupplier shouldStop, InputStream stream, Consumer<String> newLineConsumer) {
        try (
                final InputStreamReader in = new InputStreamReader(stream, StandardCharsets.UTF_8);
                final BufferedReader reader = new BufferedReader(in)) {
            String line;
            while ((line = reader.readLine()) != null && shouldStop.getAsBoolean()) {
                newLineConsumer.accept(line);
            }
        } catch (IOException e) {
            // ignore
        }
    }

}
