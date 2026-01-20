package io.mvnpm.esbuild;

import static io.mvnpm.esbuild.BundlerTestHelper.executeTest;
import static io.mvnpm.esbuild.BundlerTestHelper.getBundleOptions;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.maven.utils.Os;
import org.junit.jupiter.api.Test;

import io.mvnpm.esbuild.model.BundleOptions;
import io.mvnpm.esbuild.model.BundleResult;
import io.mvnpm.esbuild.model.WebDependency.WebDependencyType;

public class BundlerBuildTestBase {

    private static void execute() {

    }

    @Test
    void printOs() {
        System.out.println("OS Name: " + System.getProperty("os.name"));
        System.out.println("OS Architecture: " + System.getProperty("os.arch"));
        System.out.println("OS Version: " + System.getProperty("os.version"));
        System.out.println("is windows: " + Os.isFamily("windows"));
        System.out.println("is linux: " + Os.isFamily("linux"));
        System.out.println("is mac: " + Os.isFamily("mac"));
    }

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
    public void shouldThrowException() {
        assertThrows(BundlingException.class, () -> {
            executeTest(List.of("/mvnpm/stimulus-3.2.1.jar"), WebDependencyType.MVNPM, "application-error.js", false);
        });
    }

    @Test
    public void shouldThrowExceptionNodeModulesNotAncestor() {
        assertThrows(BundlingException.class, () -> {
            final Path parent = Files.createTempDirectory("foo");
            final Path work = Files.createDirectories(parent.resolve("work"));
            final BundleOptions bundleOptions = getBundleOptions(List.of(), WebDependencyType.MVNPM, "application-mvnpm.js")
                    .withWorkDir(work)
                    .withNodeModulesDir(parent.resolve("foo/node_modules")).build();
            Bundler.bundle(bundleOptions, true);
        });
    }

    @Test
    public void shouldThrowException2() {
        assertThrows(BundlingException.class, () -> {
            executeTest(List.of(), WebDependencyType.MVNPM, "simple-error.js", false);
        });
    }

    @Test
    public void simpleWarning() throws URISyntaxException, IOException {
        final BundleResult bundleResult = executeTest(List.of(), WebDependencyType.MVNPM, "simple-warning.js", false);
        assertTrue(bundleResult.logs().countWarnings() > 0);
    }

    @Test
    public void shouldResolveRelativeFolders() throws URISyntaxException, IOException {
        // given

        final Path root = BundlerTestHelper.copyTestScriptsDir("relative-path");
        final BundleOptions bundleOptions = BundleOptions.builder().withWorkDir(root)
                .addAutoEntryPoint(root, "main", List.of("foo/bar.js")).build();

        // when
        final BundleResult result = Bundler.bundle(bundleOptions, true);

        // then
        assertTrue(result.dist().toFile().exists());
    }

}
