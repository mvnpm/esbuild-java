package io.mvnpm.esbuild.resolve;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.mvnpm.esbuild.Bundler;

public class BundleResolverTest extends BundleTester {

    public static final Resolver THROWING_RESOLVER = version -> {
        throw new RuntimeException("Should not call this");
    };

    @BeforeAll
    public static void cleanUp() throws IOException {
        cleanUpDefault();
    }

    @Test
    public void resolve() throws IOException {
        // when
        final Path resolve = new BundledResolver(THROWING_RESOLVER).resolve(Bundler.ESBUILD_EMBEDDED_VERSION);
        assertTrue(Bundler.ESBUILD_EMBEDDED_VERSION.contains("mvnpm"));
        // then
        assertTrue(resolve.toFile().exists());
    }
}
