package io.mvnpm.esbuild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import io.mvnpm.esbuild.model.EsBuildConfig;
import io.mvnpm.esbuild.model.ExecuteResult;
import io.mvnpm.esbuild.resolve.ExecutableResolver;

public class ExecuteTest {

    @Test
    public void shouldExecuteEsBuild() throws IOException {
        final EsBuildConfig esBuildConfig = new EsBuildConfig();
        esBuildConfig.setVersion(true);
        final String defaultVersion = Bundler.ESBUILD_EMBEDDED_VERSION;
        final Path path = new ExecutableResolver().resolve(defaultVersion);
        final ExecuteResult executeResult = new Execute(path.toFile(), esBuildConfig).executeAndWait();
        assertEquals(defaultVersion + "\n", executeResult.output());
    }
}
