package io.mvnpm.esbuild.script;

import static io.mvnpm.esbuild.deno.DenoRunner.formatScript;

import java.io.IOException;
import java.nio.file.Path;

import io.mvnpm.esbuild.deno.DenoRunner;
import io.mvnpm.esbuild.model.BundleOptions;

public class BuildScript {

    // language=JavaScript
    private static final String SCRIPT = CommonScript.COMMON + """
            async function build () {
                const options = %s;
                console.log(`Running EsBuild (${esbuild.version})`);
                try {
                   await esbuild.build(applyPlugins({
                    ...options,
                    logLevel: "warning"
                    }));
                    console.log("Bundling completed successfully");
                    esbuild.stop();
                    process.exit(0);
                } catch(e) {
                    console.log("Error during bundling.");
                    esbuild.stop();
                    process.exit(1);
                }
            }

            await build();

            """;

    public static String build(Path workDir, Path nodeModulesDir, BundleOptions bundleOptions) {
        try {
            final String scriptContent = formatScript(SCRIPT, workDir, bundleOptions);
            return DenoRunner.runDenoScript(workDir, nodeModulesDir, scriptContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
