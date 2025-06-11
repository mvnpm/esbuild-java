package io.mvnpm.esbuild;

import static io.mvnpm.esbuild.install.BuildDependency.getDependencies;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.interop.V8Host;

import io.mvnpm.esbuild.install.WebDepsInstaller;
import io.mvnpm.esbuild.model.EsBuildConfig;
import io.mvnpm.esbuild.model.ExecuteResult;
import io.mvnpm.esbuild.model.WatchBuildResult;
import io.mvnpm.esbuild.model.WatchStartResult;
import io.mvnpm.esbuild.util.EntryCreator;

public class Execute {

    private static final ExecutorService EXECUTOR_STREAMER = Executors.newSingleThreadExecutor(r -> {
        final Thread t = new Thread(r, "Process stdout streamer");
        t.setDaemon(true);
        return t;
    });
    private static final Logger logger = Logger.getLogger(Execute.class.getName());

    private final Path workDir;
    private EsBuildConfig esBuildConfig;

    public Execute(Path workDir, EsBuildConfig esBuildConfig) {
        this.workDir = workDir;
        this.esBuildConfig = esBuildConfig;
    }

    public ExecuteResult executeAndWait() throws IOException {
        runBuild();
        Path path = workDir.resolve(esBuildConfig.outdir());
        try (Scanner scanner = new Scanner(path, StandardCharsets.UTF_8)) {
            String output = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            return new ExecuteResult(output);
        }
    }

    private void runBuild() throws IOException {
        File entryJs = EntryCreator.createEntryJs(workDir, esBuildConfig);
        WebDepsInstaller.install(workDir.resolve("node_modules"), getDependencies());

        try (NodeRuntime nodeRuntime = V8Host.getNodeInstance().createV8Runtime()) {
            nodeRuntime.allowEval(true);
            try {
                nodeRuntime.getExecutor(entryJs).executeVoid();
            } catch (JavetException e) {
                throw new RuntimeException(e);
            }
            nodeRuntime.await();
        } catch (JavetException e) {
            throw new RuntimeException(e);
        }
    }

    public WatchStartResult watch(BuildEventListener listener) throws IOException {
        runBuild();
        final ExecutorService executorStreamer = Executors
                .newSingleThreadExecutor(r -> new Thread(r, "Esbuild watch stdout streamer"));
        final ExecutorService executorBuild = Executors
                .newSingleThreadExecutor(r -> new Thread(r, "Esbuild build listeners notify"));
        final AtomicReference<WatchBuildResult> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        final WatchStartResult.WatchProcess watchProcess = new WatchStartResult.WatchProcess() {
            @Override
            public boolean isAlive() {
                return true;
            }

            @Override
            public void close() throws IOException {
                executorStreamer.shutdownNow();
                executorBuild.shutdownNow();
                if (latch.getCount() == 1) {
                    latch.countDown();
                }
            }
        };
        try {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final PrintStream printStream = new PrintStream(stream, true, StandardCharsets.UTF_8);
            System.setOut(printStream);

            executorStreamer.execute(new Streamer(executorBuild, () -> true, stream, (r) -> {
                if (latch.getCount() == 1) {
                    result.set(r);
                    latch.countDown();
                } else {
                    listener.onBuild(r);
                }
            }, r -> {
                if (latch.getCount() == 1) {
                    result.set(r);
                    latch.countDown();
                } else if (!r.isSuccess()) {
                    listener.onBuild(r);
                }
            }));
            latch.await();
            if (!result.get().isSuccess()) {
                throw result.get().bundleException();
            }

            return new WatchStartResult(result.get(), watchProcess);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            watchProcess.close();
            throw new RuntimeException(e);
        }
    }

    private record Streamer(ExecutorService executorBuild, BooleanSupplier isAlive, ByteArrayOutputStream processStream,
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

    private static void consumeStream(BooleanSupplier stayAlive, ByteArrayOutputStream stream, Consumer<String> newLineConsumer) {
        try (
                final InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(stream.toByteArray()));
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
