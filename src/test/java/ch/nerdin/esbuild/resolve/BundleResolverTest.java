package ch.nerdin.esbuild.resolve;

import ch.nerdin.esbuild.Bundler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BundleResolverTest extends BundleTester {

    public static final Resolver THROWING_RESOLVER = version -> { throw new RuntimeException("Should not call this"); };

    @BeforeAll
    public static void cleanUp() throws IOException {
        cleanUpDefault();
    }

    @Test
    public void resolve() throws IOException {
        // when
        final Path resolve = new BundledResolver(THROWING_RESOLVER).resolve(Bundler.getDefaultVersion());

        // then
        assertTrue(resolve.toFile().exists());
    }
}
