package ch.nerdin.esbuild;

import ch.nerdin.esbuild.model.EsBuildConfig;
import ch.nerdin.esbuild.model.ExecuteResult;
import ch.nerdin.esbuild.resolve.CacheResolver;
import ch.nerdin.esbuild.resolve.ExecutableResolver;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static ch.nerdin.esbuild.resolve.BundleResolverTest.THROWING_RESOLVER;
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
