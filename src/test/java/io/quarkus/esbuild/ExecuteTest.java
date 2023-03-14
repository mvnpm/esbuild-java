package io.quarkus.esbuild;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class ExecuteTest {

    @Test
    public void shouldExecuteEsBuild() throws IOException {
        final Config config = new Config();
        config.setVersion(true);
        final Path path = new Download(DownloadTest.VERSION).execute();
        new Execute(path.toFile(), config).execute();
    }
}
