package io.mvnpm.esbuild.script;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import com.caoccao.javet.enums.JSRuntimeType;
import com.caoccao.javet.enums.V8AwaitMode;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.interop.V8Locker;
import com.caoccao.javet.interop.engine.IJavetEngine;
import com.caoccao.javet.interop.engine.IJavetEnginePool;
import com.caoccao.javet.interop.engine.JavetEngineConfig;
import com.caoccao.javet.interop.engine.JavetEnginePool;
import com.caoccao.javet.values.reference.V8ValueGlobalObject;
import com.caoccao.javet.values.reference.V8ValueObject;
import com.caoccao.javet.values.reference.V8ValuePromise;

import io.mvnpm.esbuild.BundleException;
import io.mvnpm.esbuild.install.EsBuildDeps;
import io.mvnpm.esbuild.model.EsBuildConfig;

public class BuildScript {

    private static final AtomicReference<IJavetEnginePool<NodeRuntime>> POOL_REF = new AtomicReference<>();
    // language=JavaScript
    private static final String SCRIPT = """
            const esbuild = require('esbuild');
            const requiredPlugins = [];
            for(let plugin of plugins) {
                console.debug(`Adding ${plugin}`);
                requiredPlugins.push(require(plugin).default.call());
            }

            async function build () {
                const {watch, ...options} = JSON.parse(config);
                console.log(`Running esbuild (${esbuild.version})`);
                try {
                   await esbuild.build({
                    ...options,
                    logLevel: "warning",
                    plugins: requiredPlugins
                    });
                    console.log("build finished");
                    return {success: true};
                } catch(e) {
                    console.log("Error during build.");
                    return {success: false, error: e};
                }
            }
            """;

    public static void build(Path workDir, EsBuildConfig config) {
        try (IJavetEngine<NodeRuntime> javetEngine = getOrCreatePool().getEngine()) {
            try (NodeRuntime nodeRuntime = javetEngine.getV8Runtime()) {
                nodeRuntime.allowEval(true);
                try (V8Locker ignored = nodeRuntime.getV8Locker()) {
                    try (V8ValueGlobalObject globalObject = nodeRuntime.getGlobalObject()) {
                        // bind config
                        globalObject.set("plugins", EsBuildDeps.get().plugins());
                        globalObject.set("config", config.toJson());
                        globalObject.set("workDir", workDir.toString());

                        if (!globalObject.has("build")) {
                            nodeRuntime.getExecutor(SCRIPT).setResourceName(workDir.resolve("build.js").toString())
                                    .executeVoid();
                        }

                        try (V8ValuePromise promise = nodeRuntime.getGlobalObject().invoke("build")) {
                            while (!promise.isFulfilled() && !Thread.currentThread().isInterrupted()) {
                                nodeRuntime.await(V8AwaitMode.RunOnce);
                            }
                            try (final V8ValueObject result = promise.getResult()) {
                                final boolean success = result.getBoolean("success");
                                if (!success) {
                                    throw new BundleException("Error while executing Esbuild", result.getString("error"));
                                }
                            }
                        } catch (JavetException e) {
                            throw new RuntimeException("Error while preparing Esbuild script", e);
                        }
                    }
                }
            }
        } catch (JavetException e) {
            throw new RuntimeException(e);
        }

    }

    private static IJavetEnginePool<NodeRuntime> getOrCreatePool() {
        return POOL_REF.updateAndGet(existing -> {
            if (existing != null) {
                return existing;
            }
            return createPool();
        });
    }

    private static JavetEnginePool<NodeRuntime> createPool() {
        JavetEngineConfig config = new JavetEngineConfig();
        config.setJSRuntimeType(JSRuntimeType.Node);
        config.setAllowEval(true);
        return new JavetEnginePool<>(config);
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            IJavetEnginePool<NodeRuntime> pool = POOL_REF.getAndSet(null);
            if (pool != null) {
                try {
                    pool.close();
                } catch (Exception e) {
                    // Log or ignore
                }
            }
        }));
    }

}
