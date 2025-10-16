package io.mvnpm.esbuild.script;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import com.caoccao.javet.enums.V8AwaitMode;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.values.reference.V8ValueGlobalObject;
import com.caoccao.javet.values.reference.V8ValueObject;
import com.caoccao.javet.values.reference.V8ValuePromise;

import io.mvnpm.esbuild.BundleException;
import io.mvnpm.esbuild.install.EsBuildDeps;
import io.mvnpm.esbuild.model.EsBuildConfig;

/**
 * Calling build is Threadsafe as soon as init has been called before without a risk of race
 */
public class DevScript implements DevProcess {
    private final Path workDir;
    private final EsBuildConfig config;
    private final Path outDir;
    private final AtomicReference<NodeRuntime> nodeRuntime = new AtomicReference<>();
    private final AtomicReference<V8ValueGlobalObject> globalObject = new AtomicReference<>();
    private final Object lock = new Object();

    // language=JavaScript
    private static final String SCRIPT = """

                        const esbuild = require('esbuild');

            const requiredPlugins = [];

            for (let plugin of plugins) {
                console.debug(`Adding ${plugin}`);
                requiredPlugins.push(require(plugin).default.call());
            }


            let options = JSON.parse(config);

            let context = null;


            async function build() {
                console.log(`Running esbuild (${esbuild.version})`);
                try {
                    console.debug("Esbuild config:" + config);
                    if (context == null) {
                        context = await esbuild.context({
                            ...options,
                            logLevel: "warning",
                            plugins: requiredPlugins
                        });
                    }
                    let result = await context.rebuild();
                    if (result.errors.length > 0) {
                        console.debug("build finished with errors: " + result.errors);
                        return {success: false, error: result.errors[0].text};
                    }

                    return {success: true};
                } catch (e) {
                    console.debug("build exception", e);
                    return {success: false, error: e.message};
                }
            }

            async function close() {
                console.debug('Closing Esbuild Dev.');
                if (context) {
                    await context.dispose();
                    console.log('Esbuild Dev process closed.');
                }
                esbuild.stop();
                options = null;
            };


            process.on('unhandledRejection', (reason, promise) => {
                close();
            });
            """;

    public DevScript(Path workDir, EsBuildConfig config) {
        this.workDir = workDir;
        this.config = config;
        final String out = config.outdir() != null ? config.outdir() : "dist";
        this.outDir = workDir.resolve(out);
    }

    @Override
    public void init() {
        if (nodeRuntime.get() != null) {
            throw new IllegalStateException("DevScript has already been initialized");
        }
        try {
            nodeRuntime.set(V8Host.getNodeInstance().createV8Runtime());
            nodeRuntime.get().allowEval(true);
            globalObject.set(nodeRuntime.get().getGlobalObject());
            globalObject.get().set("plugins", EsBuildDeps.get().plugins());
            globalObject.get().set("config", config.toJson());
            globalObject.get().set("workDir", workDir.toString());
            nodeRuntime.get().getExecutor(SCRIPT).setResourceName(workDir.resolve("build.js").toString()).executeVoid();
        } catch (JavetException e) {
            close();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void build() throws IOException {
        if (nodeRuntime.get() == null) {
            throw new IllegalStateException("DevScript has not been initialized");
        }
        invokeBuild();
    }

    private void invokeBuild() {
        try (V8ValuePromise promise = globalObject.get().invoke("build")) {
            while (!promise.isFulfilled() && !Thread.currentThread().isInterrupted()) {
                nodeRuntime.get().await(V8AwaitMode.RunOnce);
            }
            try (final V8ValueObject result = promise.getResult()) {
                final boolean success = result.getBoolean("success");
                if (!success) {
                    throw new BundleException("Error while executing Esbuild", result.getString("error"));
                }
            }

        } catch (JavetException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Path workDir() {
        return workDir;
    }

    @Override
    public Path dist() {
        return outDir;
    }

    @Override
    public boolean isAlive() {
        return nodeRuntime.get() != null && !nodeRuntime.get().isClosed();
    }

    @Override
    public void close() {
        final NodeRuntime runtime = this.nodeRuntime.getAndSet(null);
        final V8ValueGlobalObject obj = this.globalObject.getAndSet(null);
        if (runtime != null) {
            try {
                if (obj.has("close")) {
                    try (final V8ValuePromise promise = obj.invoke("close")) {
                        while (!promise.isFulfilled() && !Thread.currentThread().isInterrupted()) {
                            runtime.await(V8AwaitMode.RunOnce);
                        }
                    }
                }
                runtime.lowMemoryNotification();
                runtime.close();
            } catch (JavetException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
