package io.mvnpm.esbuild;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import io.mvnpm.esbuild.model.EsBuildConfigBuilder;
import io.mvnpm.esbuild.script.ScriptRunner;

public class ScriptRunnerTest {

    @Test
    public void shouldExecuteEsBuild() throws IOException {
        final EsBuildConfigBuilder esBuildConfig = new EsBuildConfigBuilder();
        esBuildConfig.version();
        Path workingDirectory = Files.createTempDirectory("testBuild");
        new ScriptRunner(workingDirectory, esBuildConfig.build()).build();
    }
}
