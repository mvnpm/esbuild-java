package io.mvnpm.esbuild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import io.mvnpm.esbuild.model.EsBuildConfig;
import io.mvnpm.esbuild.model.EsBuildConfigBuilder;
import io.mvnpm.esbuild.model.ExecuteResult;
import io.mvnpm.esbuild.resolve.Resolver;

public class ExecuteTest {

    @Test
    public void shouldExecuteEsBuild() throws IOException {
        final EsBuildConfigBuilder esBuildConfig = EsBuildConfig.builder();
        esBuildConfig.version(true);
        final String defaultVersion = Bundler.ESBUILD_EMBEDDED_VERSION;
        final Path path = Resolver.create().resolve(defaultVersion);
        String workingDirectory = System.getProperty("user.dir");
        final ExecuteResult executeResult = new Execute(Paths.get(workingDirectory), path.toFile(), esBuildConfig.build())
                .executeAndWait();
        assertEquals(defaultVersion + "\n", executeResult.output());
    }
}
