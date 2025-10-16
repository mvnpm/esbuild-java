package io.mvnpm.esbuild.script;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

import io.mvnpm.esbuild.model.EsBuildConfig;

public class ScriptRunner {

    private static final Logger logger = Logger.getLogger(ScriptRunner.class.getName());

    private final Path workDir;
    private final EsBuildConfig esBuildConfig;

    public ScriptRunner(Path workDir, EsBuildConfig esBuildConfig) {
        this.workDir = workDir;
        this.esBuildConfig = esBuildConfig;
    }

    public static Path getOutDir(Path workDir, EsBuildConfig config) {
        final String out = config.outdir() != null ? config.outdir() : "dist";
        return workDir.resolve(out);
    }

    public void build() throws IOException {
        //WebDepsInstaller.install(workDir.resolve("node_modules"), getDependencies());
        BuildScript.build(workDir, esBuildConfig);
    }

    public DevProcess dev() throws IOException {
        //WebDepsInstaller.install(workDir.resolve("node_modules"), getDependencies());
        return new DevScript(workDir, esBuildConfig);
    }

}
