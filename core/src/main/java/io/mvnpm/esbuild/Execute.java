package io.mvnpm.esbuild;

import static io.mvnpm.esbuild.install.BuildDependency.getDependencies;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.interop.converters.JavetProxyConverter;

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
    private final EsBuildConfig esBuildConfig;

    public Execute(Path workDir, EsBuildConfig esBuildConfig) {
        this.workDir = workDir;
        this.esBuildConfig = esBuildConfig;
    }

    public ExecuteResult executeAndWait() throws IOException {
        runBuild();
        Path path = workDir.resolve(esBuildConfig.outdir() == null ? "." : esBuildConfig.outdir());
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

    private void runWatchBuild(BuildEventListener listener, CountDownLatch latch) throws IOException {
        File entryJs = EntryCreator.createEntryJs(workDir, esBuildConfig);
        WebDepsInstaller.install(workDir.resolve("node_modules"), getDependencies());

        try (NodeRuntime nodeRuntime = V8Host.getNodeInstance().createV8Runtime()) {
            nodeRuntime.setConverter(new JavetProxyConverter());
            nodeRuntime.allowEval(true);

            // Set up a JavaScript callback to handle build events
            nodeRuntime.getGlobalObject().set("buildEventHandler", new BuildEventHandler() {
                private boolean isFirstBuild = true;

                @Override
                public void onBuildComplete(String output) {
                    if (isFirstBuild) {
                        isFirstBuild = false;
                        latch.countDown();
                    } else {
                        listener.onBuild(new WatchBuildResult(output));
                    }
                }

                @Override
                public void onBuildError(String output, String error) {
                    if (isFirstBuild) {
                        isFirstBuild = false;
                        latch.countDown();
                    } else {
                        listener.onBuild(new WatchBuildResult(output, new BundleException(output, error)));
                    }
                }
            });

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

    public interface BuildEventHandler {
        void onBuildComplete(String output);

        void onBuildError(String output, String error);
    }

    public WatchStartResult watch(BuildEventListener listener) throws IOException {
        final ExecutorService executorBuild = Executors
                .newSingleThreadExecutor(r -> new Thread(r, "Esbuild build listeners notify"));
        CountDownLatch latch = new CountDownLatch(1);
        final WatchStartResult.WatchProcess watchProcess = getProcess(executorBuild, latch);

        try {
            executorBuild.execute(() -> {
                try {
                    runWatchBuild(listener, latch);
                } catch (IOException e) {
                    if (latch.getCount() == 1) {
                        latch.countDown();
                    }
                }
            });
            latch.await();

            return new WatchStartResult(new WatchBuildResult(""), watchProcess);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            watchProcess.close();
            throw new RuntimeException(e);
        }
    }

    private static WatchStartResult.WatchProcess getProcess(ExecutorService executorBuild, CountDownLatch latch) {
        final AtomicBoolean alive = new AtomicBoolean(true);
        return new WatchStartResult.WatchProcess() {
            @Override
            public boolean isAlive() {
                return alive.get();
            }

            @Override
            public void close() throws IOException {
                alive.set(false);
                executorBuild.shutdownNow();
                if (latch.getCount() == 1) {
                    latch.countDown();
                }
            }
        };
    }

}
