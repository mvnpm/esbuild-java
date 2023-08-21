package io.mvnpm.esbuild;

import io.mvnpm.esbuild.model.BundleOptions;
import io.mvnpm.esbuild.model.BundleOptionsBuilder;
import io.mvnpm.esbuild.model.BundleResult;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BundlerTest {

    @Test
    public void shouldBundleMvnpm() throws URISyntaxException, IOException {
        executeTest("/mvnpm/stimulus-3.2.1.jar", Bundler.BundleType.MVNPM, "application-mvnpm.js", true);
    }

    @Test
    public void shouldBundle() throws URISyntaxException, IOException {
        executeTest("/webjars/htmx.org-1.8.4.jar", Bundler.BundleType.WEBJARS, "application-webjar.js", true);
    }

    @Test
    public void shouldWatch() throws URISyntaxException, IOException, InterruptedException {
        // given
        final BundleOptions options = getBundleOptions("/mvnpm/stimulus-3.2.1.jar", Bundler.BundleType.MVNPM, "application-mvnpm.js");

        // when
        AtomicBoolean isCalled = new AtomicBoolean(false);
        final Watch watch = Bundler.watch(options, () -> isCalled.set(true));

        // then
        Thread.sleep(2000);
        watch.stop();
        assertTrue(isCalled.get());
    }

    @Test
    public void shouldThrowException() {
        assertThrows(BundleException.class, () -> {
            executeTest("/mvnpm/stimulus-3.2.1.jar", Bundler.BundleType.MVNPM, "application-error.js", false);
        });
    }

    @Test
    public void shouldResolveRelativeFolders() throws URISyntaxException, IOException {
        // given
        final Path root = new File(getClass().getResource("/path/").toURI()).toPath();
        final BundleOptions bundleOptions = new BundleOptionsBuilder().setWorkFolder(root).addAutoEntryPoint(root,"main", List.of("foo/bar.js")).build();

        // when
        final BundleResult result = Bundler.bundle(bundleOptions);

        // then
        assertTrue(result.dist().toFile().exists());
    }

    private void executeTest(String jarName, Bundler.BundleType type, String scriptName, boolean check) throws URISyntaxException, IOException {
        final BundleOptions bundleOptions = getBundleOptions(jarName, type, scriptName);
        final BundleResult result = Bundler.bundle(bundleOptions);

        if (check) {
            assertTrue(result.dist().toFile().exists());
        }

    }

    private BundleOptions getBundleOptions(String jarName, Bundler.BundleType type, String scriptName) throws URISyntaxException {
        final File jar = new File(getClass().getResource(jarName).toURI());
        final List<Path> dependencies = Collections.singletonList(jar.toPath());
        final Path rootDir =  new File(getClass().getResource("/").toURI()).toPath();
        return new BundleOptionsBuilder().withDependencies(dependencies)
                .addEntryPoint(rootDir, scriptName).withType(type).build();
    }

}
