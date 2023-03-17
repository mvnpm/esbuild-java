package ch.nerdin.esbuild.resolve;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CacheResolverTest {

    @Test
    public void notResolve() throws IOException {
        // given
        String version = "dummy";
        Files.createTempDirectory("esbuild-" + version);

        // when
        final Path path = new CacheResolver(version1 -> Path.of("/")).resolve(version);

        //then
        assertEquals(Path.of("/"), path);
    }

    @Test
    public void resolve() throws IOException {
        // given
        String version = "other";
        final Path tempDirectory = Files.createTempDirectory("esbuild-" + version);
        final Path resolve = tempDirectory.resolve(BaseResolver.EXECUTABLE_PATH);
        resolve.toFile().mkdirs();

        // when
        final Path path = new CacheResolver(null).resolve(version);

        //then
        assertEquals(resolve, path);
    }
}
