package ch.nerdin.esbuild;

import ch.nerdin.esbuild.modal.EsBuildConfig;
import ch.nerdin.esbuild.resolve.CacheResolver;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class ExecuteTest {

    @Test
    public void shouldExecuteEsBuild() throws IOException {
        final EsBuildConfig esBuildConfig = new EsBuildConfig();
        esBuildConfig.setVersion(true);
        final Path path = new CacheResolver(null).resolve(Bundler.getDefaultVersion());
        new Execute(path.toFile(), esBuildConfig).executeAndWait();
    }
}
