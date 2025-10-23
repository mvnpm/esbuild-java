package io.mvnpm.esbuild.script;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

import io.mvnpm.esbuild.model.BundleOptions;
import io.mvnpm.esbuild.model.EsBuildConfig;

public class ScriptRunner {

    private static final Logger logger = Logger.getLogger(ScriptRunner.class.getName());

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

    public String build() throws IOException {
        return BuildScript.build(workDir, nodeModulesDir, bundleOptions);
    }

    public DevProcess dev() throws IOException {
        return new DevScript(workDir, bundleOptions);
    }

}
