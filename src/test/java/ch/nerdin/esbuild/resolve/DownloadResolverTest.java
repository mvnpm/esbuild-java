package ch.nerdin.esbuild.resolve;

import ch.nerdin.esbuild.BundleDependencies;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DownloadResolverTest {

    @Test
    public void download() throws IOException {
        // when
        final Path path = new DownloadResolver(null).resolve(BundleDependencies.ESBUILD_VERSION);

        // then
        assertTrue(path.toFile().exists());
    }
}
