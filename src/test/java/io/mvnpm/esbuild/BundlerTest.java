package io.mvnpm.esbuild;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import io.mvnpm.esbuild.model.*;
import io.mvnpm.esbuild.model.WebDependency.WebDependencyType;

public class BundlerTest {

    @Test
    public void shouldBundleMvnpm() throws URISyntaxException, IOException {
        executeTest(List.of("/mvnpm/stimulus-3.2.1.jar"), WebDependencyType.MVNPM, "application-mvnpm.js", true);
    }

    @Test
    public void shouldBundleMvnpmSources() throws URISyntaxException, IOException {
        executeTest(List.of("/mvnpm/moment-2.29.4-sources.jar"), WebDependencyType.MVNPM,
                "application-mvnpm.ts", true);
    }

    @Test
    void shouldBundleWithScss() throws IOException, URISyntaxException {
        final Path root = new File(getClass().getResource("/scss/").toURI()).toPath();

        final BundleOptions options = BundleOptions.builder()
                .withWorkDir(root)
                .withEsConfig(EsBuildConfig.builder().entryNames("[name]").build())
                .withDependencies(getJars(List.of("/mvnpm/stimulus-3.2.1.jar", "/mvnpm/bootstrap-5.2.3.jar")),
                        WebDependencyType.MVNPM)
                .addEntryPoint("app.js").build();

        Bundler.bundle(options, true);

        assertTrue(options.workDir().resolve("dist").resolve("app.js").toFile().exists());
        assertTrue(options.workDir().resolve("dist").resolve("app.css").toFile().exists());

    }

    @Test
    public void shouldBundleMvnpmAndCreatePackageJson() throws URISyntaxException, IOException {
        executeTest(List.of("/mvnpm/stimulus-3.2.0.jar"), WebDependencyType.MVNPM, "application-mvnpm.js", true);
    }

    @Test
    public void shouldBundleMvnpmWithoutPackageJson() throws URISyntaxException, IOException {
        executeTest(List.of("/mvnpm/polymer-3.5.1.jar"), WebDependencyType.MVNPM, "application-mvnpm-importmap.js", true);
    }

    @Test
    public void shouldBundle() throws URISyntaxException, IOException {
        executeTest(List.of("/webjars/htmx.org-1.8.4.jar"), WebDependencyType.WEBJARS, "application-webjar.js", true);
    }

    @Test
    public void shouldWatch() throws URISyntaxException, IOException, InterruptedException {
        // given
        final BundleOptions options = getBundleOptions(List.of("/mvnpm/stimulus-3.2.1.jar"),
                WebDependencyType.MVNPM,
                "application-mvnpm.js").withEsConfig(EsBuildConfig.builder().fixedEntryNames().build()).build();

        // when
        AtomicReference<CountDownLatch> latch = new AtomicReference<>(new CountDownLatch(1));
        AtomicReference<BundleException> bundleException = new AtomicReference<>();
        final Watch watch = Bundler.watch(options, (error) -> {
            error.ifPresent(bundleException::set);
            latch.get().countDown();
        }, true);

        // then
        assertTrue(latch.get().await(2, TimeUnit.SECONDS));
        assertNull(bundleException.get(), "No error during bundling");
        final Path app = watch.workDir().resolve("application-mvnpm.js");
        assertTrue(Files.exists(app));
        final Path distApp = watch.dist().resolve("application-mvnpm.js");
        assertTrue(Files.exists(distApp));

        //  when
        latch.set(new CountDownLatch(1));
        Files.writeString(app, "\nalert(\"foo\");", StandardOpenOption.APPEND);
        assertTrue(latch.get().await(2, TimeUnit.SECONDS));
        assertNull(bundleException.get(), "No error during bundling");

        assertTrue(Files.readString(distApp).contains("alert(\"foo\");"));

        // then
        watch.stop();
    }

    @Test
    public void shouldWatchWithError() throws URISyntaxException, IOException, InterruptedException {

        // given
        final BundleOptions options = getBundleOptions(List.of("/mvnpm/stimulus-3.2.1.jar"),
                WebDependencyType.MVNPM,
                "application-error.js").withEsConfig(EsBuildConfig.builder().fixedEntryNames().build()).build();

        // when
        AtomicReference<CountDownLatch> latch = new AtomicReference<>(new CountDownLatch(1));
        AtomicReference<BundleException> bundleException = new AtomicReference<>();
        final Watch watch = Bundler.watch(options, (error) -> {
            error.ifPresent(bundleException::set);
            latch.get().countDown();
        }, true);

        // then
        assertTrue(latch.get().await(2, TimeUnit.SECONDS));
        assertNotNull(bundleException.get(), "Error during bundling");
        assertTrue(bundleException.get().output().contains("[ERROR] Could not resolve \"\""));

        final Path app = watch.workDir().resolve("application-error.js");
        assertTrue(Files.exists(app));

        // when
        bundleException.set(null);
        latch.set(new CountDownLatch(1));
        Files.writeString(app, "alert(\"foo\");", StandardOpenOption.TRUNCATE_EXISTING);

        // then
        assertTrue(latch.get().await(2, TimeUnit.SECONDS));
        assertNull(bundleException.get(), "No error during bundling");
        final Path distApp = watch.dist().resolve("application-error.js");
        assertTrue(Files.exists(distApp));
        assertTrue(Files.readString(distApp).contains("alert(\"foo\");"));

        // then
        watch.stop();
    }

    @Test
    public void shouldThrowException() {
        assertThrows(BundleException.class, () -> {
            executeTest(List.of("/mvnpm/stimulus-3.2.1.jar"), WebDependencyType.MVNPM, "application-error.js", false);
        });
    }

    @Test
    public void shouldResolveRelativeFolders() throws URISyntaxException, IOException {
        // given
        final Path root = new File(getClass().getResource("/path/").toURI()).toPath();
        final BundleOptions bundleOptions = BundleOptions.builder().withWorkDir(root)
                .addAutoEntryPoint(root, "main", List.of("foo/bar.js")).build();

        // when
        final BundleResult result = Bundler.bundle(bundleOptions, true);

        // then
        assertTrue(result.dist().toFile().exists());
    }

    private void executeTest(List<String> jarNames, WebDependencyType type, String scriptName, boolean check)
            throws URISyntaxException, IOException {
        final BundleOptions bundleOptions = getBundleOptions(jarNames, type, scriptName).build();
        final BundleResult result = Bundler.bundle(bundleOptions, true);

        if (check) {
            assertTrue(result.dist().toFile().exists());
        }

    }

    private BundleOptionsBuilder getBundleOptions(List<String> jarNames, WebDependencyType type, String scriptName)
            throws URISyntaxException {
        final List<Path> jars = getJars(jarNames);
        final Path rootDir = new File(getClass().getResource("/").toURI()).toPath();
        final BundleOptionsBuilder bundleOptionsBuilder = BundleOptions.builder().withDependencies(jars, type);
        if (scriptName != null) {
            bundleOptionsBuilder
                    .addEntryPoint(rootDir, scriptName);
        }
        return bundleOptionsBuilder;
    }

    private List<Path> getJars(List<String> jarNames) {
        final List<Path> jars = jarNames.stream().map(jarName -> {
            try {
                final URL resource = getClass().getResource(jarName);
                if (resource == null) {
                    throw new RuntimeException("Could not find resource: " + jarName);
                }
                return new File(resource.toURI()).toPath();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }).toList();
        return jars;
    }

}
