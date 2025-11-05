package io.mvnpm.esbuild.script;

import java.io.IOException;
import java.nio.file.Path;

import org.jboss.logging.Logger;

import io.mvnpm.esbuild.deno.ScriptLog;
import io.mvnpm.esbuild.model.BundleOptions;
import io.mvnpm.esbuild.model.EsBuildConfig;

public class ScriptRunner {

    private static final Logger LOG = Logger.getLogger(ScriptRunner.class);

    private final Path workDir;
    private final Path nodeModulesDir;
    private final BundleOptions bundleOptions;

    public ScriptRunner(Path workDir, Path nodeModulesDir, BundleOptions bundleOptions) {
        this.workDir = workDir;
        this.nodeModulesDir = nodeModulesDir;
        this.bundleOptions = bundleOptions;
    }

    public static Path getOutDir(Path workDir, EsBuildConfig config) {
        final String out = config.outdir() != null ? config.outdir() : "dist";
        return workDir.resolve(out);
    }

    public ScriptLog build() throws IOException {
        return BuildScript.build(workDir, nodeModulesDir, bundleOptions);
    }

    public DevProcess dev() throws IOException {
        return new DevScript(workDir, bundleOptions);
    }

}
