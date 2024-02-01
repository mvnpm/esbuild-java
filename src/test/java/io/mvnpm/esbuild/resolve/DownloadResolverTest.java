package io.mvnpm.esbuild.resolve;

import static io.mvnpm.esbuild.resolve.BundleResolverTest.THROWING_RESOLVER;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DownloadResolverTest extends BundleTester {
    private static final String TEST_VERSION = "0.19.9";

    @BeforeAll
    public static void cleanUp() throws IOException {
        cleanUp(TEST_VERSION);
    }

    @Test
    public void download() throws IOException {
        // when
        final Path path = new DownloadResolver(THROWING_RESOLVER).resolve(TEST_VERSION);

        // then
        assertTrue(Files.exists(path));
    }
}
