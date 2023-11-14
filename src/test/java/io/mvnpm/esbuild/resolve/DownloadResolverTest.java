package io.mvnpm.esbuild.resolve;

import static io.mvnpm.esbuild.resolve.BundleResolverTest.THROWING_RESOLVER;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.mvnpm.esbuild.Bundler;

public class DownloadResolverTest extends BundleTester {

    @BeforeAll
    public static void cleanUp() throws IOException {
        cleanUpDefault();
    }

    @Test
    public void download() throws IOException {
        // when
        final Path path = new DownloadResolver(THROWING_RESOLVER).resolve(Bundler.ESBUILD_EMBEDDED_VERSION);

        // then
        assertTrue(path.toFile().exists());
    }
}
