package ch.nerdin.esbuild;

import ch.nerdin.esbuild.modal.BundleOptions;
import ch.nerdin.esbuild.modal.BundleOptionsBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BundlerTest {

    @Test
    public void shouldBundleMvnpm() throws URISyntaxException, IOException {
        executeTest("/stimulus-3.2.1.jar", Bundler.BundleType.MVNPM, "/application-mvnpm.js", true);
    }

    @Test
    public void shouldBundle() throws URISyntaxException, IOException {
        executeTest("/htmx.org-1.8.4.jar", Bundler.BundleType.WEBJARS, "/application-webjar.js", true);
    }

    @Test
    public void shouldWatch() throws URISyntaxException, IOException, InterruptedException {
        // given
        final BundleOptions options = getBundleOptions("/stimulus-3.2.1.jar", Bundler.BundleType.MVNPM, "/application-mvnpm.js");

        // when
        AtomicBoolean isCalled = new AtomicBoolean(false);
        final Watch watch = Bundler.watch(options, () -> isCalled.set(true));

        // then
        Thread.sleep(2000);
        watch.stop();
        assertTrue(isCalled.get());
    }

    @Test
    public void shouldTrowException() throws IOException, URISyntaxException {
        try {
            executeTest("/stimulus-3.2.1.jar", Bundler.BundleType.MVNPM, "/application-error.js", false);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("ERROR"));
        }
    }

    @Test
    public void shouldResolveRelativeFolders() throws URISyntaxException, IOException {
        // given
        final Path root = new File(getClass().getResource("/path/").toURI()).toPath();
        final Path script = new File(getClass().getResource("/path/foo/bar.js").toURI()).toPath();
        final BundleOptions bundleOptions = new BundleOptionsBuilder().setWorkFolder(root).addEntryPoint("main", List.of(script)).build();

        // when
        final Path result = Bundler.bundle(bundleOptions);

        // then
        assertTrue(result.resolve("dist").toFile().exists());
    }

    private void executeTest(String jarName, Bundler.BundleType type, String scriptName, boolean check) throws URISyntaxException, IOException {
        final BundleOptions bundleOptions = getBundleOptions(jarName, type, scriptName);
        final Path path = Bundler.bundle(bundleOptions);

        if (check)
            assertTrue(path.toFile().exists());
    }

    private BundleOptions getBundleOptions(String jarName, Bundler.BundleType type, String scriptName) throws URISyntaxException {
        final File jar = new File(getClass().getResource(jarName).toURI());
        final List<Path> dependencies = Collections.singletonList(jar.toPath());
        final Path entry = new File(getClass().getResource(scriptName).toURI()).toPath();

        return new BundleOptionsBuilder().withDependencies(dependencies)
                .addEntryPoint(entry).withType(type).build();
    }

}
