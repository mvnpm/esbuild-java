package io.mvnpm.esbuild.script;

import static io.mvnpm.esbuild.deno.DenoRunner.formatScript;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.logging.Logger;

import io.mvnpm.esbuild.deno.DenoRunner;
import io.mvnpm.esbuild.deno.ScriptLog;
import io.mvnpm.esbuild.model.BundleOptions;

/**
 * Calling build is Threadsafe as soon as init has been called before without a risk of race
 */
public class DevScript implements DevProcess {
    private static final Logger LOG = Logger.getLogger(DevScript.class);
    private final Path workDir;
    private final BundleOptions bundleOptions;
    private final Path outDir;
    private final AtomicReference<Process> process = new AtomicReference<>();
    private final ReentrantLock lock = new ReentrantLock();

    // language=JavaScript
    private static final String SCRIPT = CommonScript.COMMON + """
            const options = %s;
            let context = null;

            async function build () {
                console.log("--BUILD--")
                console.debug(`[DEBUG] Running EsBuild (${esbuild.version})`);
                try {
                    if (context == null) {
                        context = await esbuild.context(applyPlugins(options));
                    }
                    await context.rebuild();
                    console.log("[DEBUG] Bundling completed successfully");
                    console.log("--BUILD-SUCCESS--");
                } catch (err) {
                    if (!err.errors) {
                        // We only print non bundling error, because bundling errors are already printed
                        console.log("[ERROR] EsBuild Error: " + cleanLog(err.message));
                    }
                    console.log("--BUILD-ERROR--");
                }
            }

            async function close() {
                console.log('[DEBUG] Closing Esbuild Dev.');
                if (context) {
                    await context.dispose();
                    context = null;
                    console.log('[DEBUG] Esbuild Dev closed.');
                }
                esbuild.stop();
                process.exit(0);
            };

            const decoder = new TextDecoder();
            const reader = Deno.stdin.readable.getReader();

            const handlers = {
              BUILD: async () => await build(),
              CLOSE: async () => await close()
            };

            async function listenForTriggers() {
              console.log("[DEBUG] Deno script is listening for Java events...");
              console.log("--READY--");
              try {
                while (true) {
                  const { value, done } = await reader.read();
                  if (done) break;

                  const message = decoder.decode(value).trim();
                  if (!message) continue;

                  console.log("[DEBUG] Deno script request received:", message);

                  const [trigger, ...rest] = message.split(" ");
                  const payload = rest.join(" ");

                  const handler = handlers[trigger.toUpperCase()];
                  if (handler) {
                    try {
                      await handler(payload);
                    } catch (err) {
                      console.log(`[ERROR] Handler for ${trigger} failed:`, cleanLog(err.stack));
                    }
                  } else {
                    console.error(`[ERROR] Unknown trigger: "${trigger}"`);
                  }
                }
              } catch (err) {
                console.error("[ERROR] Error while reading stdin:", cleanLog(err.stack));
              } finally {
                reader.releaseLock();
                console.log("[INFO] Listener stopped.");
              }
            }

            await listenForTriggers();
            """;

    public DevScript(Path workDir, BundleOptions bundleOptions) {
        this.workDir = workDir;
        this.bundleOptions = bundleOptions;
        final String out = bundleOptions.esBuildConfig().outdir() != null ? bundleOptions.esBuildConfig().outdir() : "dist";
        this.outDir = workDir.resolve(out);
    }

    @Override
    public void init() {
        if (process.get() != null) {
            throw new IllegalStateException("DevScript has already been initialized");
        }
        lock.lock();
        Thread destroyHook = null;
        try {
            final String scriptContent = formatScript(SCRIPT, workDir, bundleOptions);
            final Process p = DenoRunner.devDenoScript(workDir, bundleOptions.nodeModulesDir(), scriptContent,
                    bundleOptions.debugBuild());
            process.set(p);
            destroyHook = new Thread(p::destroyForcibly);
            Runtime.getRuntime().addShutdownHook(destroyHook);
            final ScriptLog log = DenoRunner.waitForResult(p, bundleOptions.debugBuild() ? -1 : bundleOptions.timeoutSeconds());
            log.logAll();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (destroyHook != null) {
                Runtime.getRuntime().removeShutdownHook(destroyHook);
            }
            lock.unlock();
        }
    }

    @Override
    public void build() throws IOException {
        if (process.get() == null) {
            throw new IllegalStateException("DevScript has not been initialized");
        }
        if (!isAlive()) {
            throw new IOException("DevScript process is closed");
        }
        lock.lock();
        try {
            invokeBuild();
        } finally {
            lock.unlock();
        }
    }

    private void invokeBuild() {
        final Process p = process.get();
        try {
            BufferedWriter bufferedWriter = p.outputWriter();
            bufferedWriter.write("BUILD");
            bufferedWriter.flush();
            final ScriptLog log = DenoRunner.waitForResult(p, bundleOptions.debugBuild() ? -1 : bundleOptions.timeoutSeconds());
            log.logAll();
        } catch (IOException e) {
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
        return process.get() != null && process.get().isAlive();
    }

    @Override
    public void close() {
        lock.lock();
        try {
            final Process p = this.process.getAndSet(null);
            if (p != null && p.isAlive()) {
                try (BufferedWriter bufferedWriter = p.outputWriter()) {
                    bufferedWriter.write("CLOSE");
                    bufferedWriter.flush();
                    final ScriptLog log = DenoRunner.waitForExit(p, 3);
                    log.logAll();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {
            lock.unlock();
        }
    }

}
