package ch.nerdin.esbuild;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BundlerTest {

    @Test
    public void shouldBundleMvnpm() throws URISyntaxException, IOException {
        executeTest("/stimulus-3.2.1-mvnpm.jar", Bundler.BundleType.MVNPM, "/application-mvnpm.js");
    }

    @Test
    public void shouldBundle() throws URISyntaxException, IOException {
        executeTest("/htmx.org-1.8.4.jar", Bundler.BundleType.WEBJARS, "/application-webjar.js");
    }

    private void executeTest(String jarName, Bundler.BundleType type, String scriptName) throws URISyntaxException, IOException {
        final File jar = new File(getClass().getResource(jarName).toURI());
        final List<Path> dependencies = Collections.singletonList(jar.toPath());
        final Path entry = new File(getClass().getResource(scriptName).toURI()).toPath();

        final Path path = Bundler.bundle(dependencies, type, entry);

        assertTrue(path.toFile().exists());
    }
}
