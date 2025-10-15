package io.mvnpm.esbuild.resolve;

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
        final Path path = new DownloadResolver().resolve(TEST_VERSION);

        // then
        assertTrue(Files.exists(path), path + " does not exist");
    }
}
