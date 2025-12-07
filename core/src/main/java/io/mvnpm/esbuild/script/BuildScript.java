package io.mvnpm.esbuild.script;

import static io.mvnpm.esbuild.deno.DenoRunner.formatScript;

import java.io.IOException;
import java.nio.file.Path;

import io.mvnpm.esbuild.deno.DenoRunner;
import io.mvnpm.esbuild.deno.ScriptLog;
import io.mvnpm.esbuild.model.BundleOptions;

public class BuildScript {

    // language=JavaScript
    private static final String SCRIPT = CommonScript.COMMON + """
            async function build () {
                const options = %s;
                console.log(`[DEBUG] Running EsBuild (${esbuild.version})`);
                try {
                   await esbuild.build(applyPlugins(options));
                    console.log("[DEBUG] Bundling completed successfully");
                    esbuild.stop();
                    process.exit(0);
                } catch(err) {
                    if (!err.errors) {
                        // We only print non bundling error, because bundling errors are already printed
                        console.log("[ERROR] EsBuild Error: " + cleanLog(err.message));
                    }
                    esbuild.stop();
                    process.exit(1);
                }
            }

            await build();

            """;

    public static ScriptLog build(Path workDir, Path nodeModulesDir, BundleOptions bundleOptions) {
        try {
            final String scriptContent = formatScript(SCRIPT, workDir, bundleOptions);
            return DenoRunner.runDenoScript(workDir, nodeModulesDir, scriptContent, bundleOptions.timeoutSeconds());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
