package io.mvnpm.esbuild;

import io.mvnpm.esbuild.model.EsBuildConfig;
import io.mvnpm.esbuild.model.ExecuteResult;
import io.mvnpm.esbuild.resolve.ExecutableResolver;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExecuteTest {

    @Test
    public void shouldExecuteEsBuild() throws IOException {
        final EsBuildConfig esBuildConfig = new EsBuildConfig();
        esBuildConfig.setVersion(true);
        final String defaultVersion = Bundler.getDefaultVersion();
        final Path path = new ExecutableResolver().resolve(defaultVersion);
        final ExecuteResult executeResult = new Execute(path.toFile(), esBuildConfig).executeAndWait();
        assertEquals(defaultVersion + "\n", executeResult.output());
    }
}
