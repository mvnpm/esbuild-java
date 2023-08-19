package io.mvnpm.esbuild.resolve;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.mvnpm.esbuild.resolve.BaseResolver.EXECUTABLE_PATH;
import static io.mvnpm.esbuild.resolve.BundleResolverTest.THROWING_RESOLVER;
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
        final Path binary = createEsBuildBinary(version);
        final Path location = BaseResolver.getLocation(version);

        // when
        final Path path = new CacheResolver(version1 -> Path.of("/")).resolve(version);

        //then
        assertEquals(binary, path);
        assertTrue(path.startsWith(location));
    }

    @Test
    public void resolve() throws IOException {
        // given
        String version = "other";
        final Path binary = createEsBuildBinary(version);
        final Path location = BaseResolver.getLocation(version);
        // when
        final Path path = new CacheResolver(THROWING_RESOLVER).resolve(version);

        //then
        assertEquals(binary, path);
        assertTrue(path.startsWith(location));
        assertTrue(location.toFile().list().length > 0);
    }

    private Path createEsBuildBinary(String version) throws IOException {
        final Path destination = BaseResolver.createDestination(version);
        final Path exec = destination.resolve(EXECUTABLE_PATH);
        Files.createDirectories(exec.getParent());
        Files.writeString(exec, "hello");
        return exec;
    }
}
