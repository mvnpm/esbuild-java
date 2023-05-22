package ch.nerdin.esbuild.resolve;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CacheResolverTest extends BundleTester {


    @AfterAll
    public static void cleanUp() throws IOException {
        cleanUp("dummy");
        cleanUp("other");
    }

    @Test
    public void notResolve() throws IOException {
        // given
        String version = "dummy";
        final Path folder = createEsBuildFolder(version);

        // when
        final Path path = new CacheResolver(version1 -> Path.of("/")).resolve(version);

        //then
        assertTrue(path.startsWith(folder));
    }

    @Test
    public void resolve() throws IOException {
        // given
        String version = "other";
        final Path folder = createEsBuildFolder(version);
        final Path resolve = folder.resolve(BaseResolver.EXECUTABLE_PATH);
        resolve.toFile().mkdirs();

        // when
        final Path path = new CacheResolver(null).resolve(version);

        //then
        assertEquals(resolve, path);
        assertTrue(path.startsWith(folder));
        assertTrue(folder.toFile().list().length > 0);
    }

    private Path createEsBuildFolder(String version) throws IOException {
        final BaseResolver resolver = new BaseResolver(null) { };
        return resolver.createDestination(version);
    }
}
