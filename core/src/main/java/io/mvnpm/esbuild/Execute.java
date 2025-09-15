package io.mvnpm.esbuild;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.mvnpm.esbuild.model.EsBuildConfig;
import io.mvnpm.esbuild.model.ExecuteResult;
import io.mvnpm.esbuild.model.WatchBuildResult;
import io.mvnpm.esbuild.model.WatchStartResult;

public class Execute {

    private static final ExecutorService EXECUTOR_STREAMER = Executors.newSingleThreadExecutor(r -> {
        final Thread t = new Thread(r, "Process stdout streamer");
        t.setDaemon(true);
        return t;
    });
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

    public WatchStartResult watch(BuildEventListener listener) throws IOException {
        final Process process = createProcess(getCommand(), Optional.of(listener));
        final ExecutorService executorStreamer = Executors
                .newSingleThreadExecutor(r -> new Thread(r, "Esbuild watch stdout streamer"));
        final ExecutorService executorBuild = Executors
                .newSingleThreadExecutor(r -> new Thread(r, "Esbuild build listeners notify"));
        final AtomicReference<WatchBuildResult> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        final WatchStartResult.WatchProcess watchProcess = new WatchStartResult.WatchProcess() {
            @Override
            public boolean isAlive() {
                return process.isAlive();
            }

            @Override
            public void close() throws IOException {
                process.destroyForcibly();
                executorStreamer.shutdownNow();
                executorBuild.shutdownNow();
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                if (latch.getCount() == 1) {
                    latch.countDown();
                }
            }
        };
        try {
            final InputStream processStream = process.getInputStream();
            executorStreamer.execute(new Streamer(executorBuild, process::isAlive, processStream, (r) -> {
                if (latch.getCount() == 1) {
                    result.set(r);
                    latch.countDown();
                } else {
                    listener.onBuild(r);
                }
            }, r -> {
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                if (latch.getCount() == 1) {
                    result.set(r);
                    latch.countDown();
                } else if (!r.isSuccess()) {
                    listener.onBuild(r);
                }
            }));
            latch.await();
            if (!process.isAlive() && !result.get().isSuccess()) {
                throw result.get().bundleException();
            }
            return new WatchStartResult(result.get(), watchProcess);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            watchProcess.close();
            throw new RuntimeException(e);
        }
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
        return new ProcessBuilder().redirectErrorStream(listener.isPresent()).directory(workDir.toFile())
                .command(command).start();
    }

    private record Streamer(ExecutorService executorBuild, BooleanSupplier isAlive, InputStream processStream,
            BuildEventListener listener, Consumer<WatchBuildResult> onExit) implements Runnable {

        @Override
        public void run() {
            final AtomicBoolean hasError = new AtomicBoolean();
            final StringBuilder outputBuilder = new StringBuilder();
            consumeStream(isAlive, processStream, l -> {
                logger.fine(l);
                outputBuilder.append("\n").append(l);
                if (l.contains("build finished")) {
                    logger.fine("Build finished!");
                    final String output = outputBuilder.toString();
                    final boolean error = hasError.getAndSet(false);
                    outputBuilder.setLength(0);
                    executorBuild.execute(() -> {
                        if (!error) {
                            listener.onBuild(new WatchBuildResult(output));
                        } else {
                            listener.onBuild(
                                    new WatchBuildResult(output, new BundleException("Error during bundling", output)));
                        }
                    });
                } else if (l.contains("[ERROR]")) {
                    hasError.set(true);
                }
            });
            if (!hasError.get()) {
                onExit.accept(new WatchBuildResult(outputBuilder.toString()));
            } else {
                onExit.accept(new WatchBuildResult(outputBuilder.toString(),
                        new BundleException("Process exited with error", outputBuilder.toString())));
            }
        }
    }

    private static String readStream(InputStream stream) {
        final StringBuilder s = new StringBuilder();
        consumeStream(() -> true, stream, l -> s.append(l).append("\n"));
        return s.toString();
    }

    private static void consumeStream(BooleanSupplier stayAlive, InputStream stream, Consumer<String> newLineConsumer) {
        try (
                final InputStreamReader in = new InputStreamReader(stream, StandardCharsets.UTF_8);
                final BufferedReader reader = new BufferedReader(in)) {
            String line;
            while ((line = reader.readLine()) != null) {
                newLineConsumer.accept(line);
                if (!stayAlive.getAsBoolean()) {
                    break;
                }
            }
        } catch (IOException e) {
            // ignore
        }
    }

}
