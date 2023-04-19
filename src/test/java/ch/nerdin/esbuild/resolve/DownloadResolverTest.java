package ch.nerdin.esbuild.resolve;

import ch.nerdin.esbuild.Bundler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DownloadResolverTest {

    @Test
    public void download() throws IOException {
        // when
        final Path path = new DownloadResolver(null).resolve(Bundler.getDefaultVersion());

        // then
        assertTrue(path.toFile().exists());
    }
}
