package io.mvnpm.esbuild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import io.mvnpm.esbuild.model.EsBuildConfig;
import io.mvnpm.esbuild.model.EsBuildConfigBuilder;
import io.mvnpm.esbuild.model.ExecuteResult;

public class ExecuteTest {

    @Test
    public void shouldExecuteEsBuild() throws IOException {
        final EsBuildConfigBuilder esBuildConfig = EsBuildConfig.builder();
        esBuildConfig.version(true);
        Path workingDirectory = Files.createTempDirectory("testBuild");
        System.out.println("workingDirectory = " + workingDirectory);
        final ExecuteResult executeResult = new Execute(workingDirectory, esBuildConfig.build())
                .executeAndWait();
        assertEquals("\n", executeResult.output());
    }
}
