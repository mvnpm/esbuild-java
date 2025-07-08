package io.mvnpm.esbuild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import io.mvnpm.esbuild.model.EsBuildConfigBuilder;
import io.mvnpm.esbuild.model.ExecuteResult;

public class ExecuteTest {

    @Test
    public void shouldExecuteEsBuild() throws IOException {
        final EsBuildConfigBuilder esBuildConfig = new EsBuildConfigBuilder();
        esBuildConfig.version();
        Path workingDirectory = Files.createTempDirectory("testBuild");
        final ExecuteResult executeResult = new Execute(workingDirectory, esBuildConfig.build())
                .executeAndWait();
        assertEquals("", executeResult.output());
    }
}
