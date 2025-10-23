package io.mvnpm.esbuild;

import static io.mvnpm.esbuild.BundlerTestHelper.getJars;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import io.mvnpm.esbuild.model.BundleOptions;
import io.mvnpm.esbuild.model.DevResult;
import io.mvnpm.esbuild.model.EsBuildConfig;
import io.mvnpm.esbuild.model.WebDependency.WebDependencyType;
import io.mvnpm.esbuild.plugin.EsBuildPluginSass;

public class BundlerScssTest {

    @Test
    void shouldBundleWithScss() throws IOException, URISyntaxException {

        final Path root = prepareTest();

        final BundleOptions options = BundleOptions.builder()
                .withWorkDir(root)
                .withEsConfig(EsBuildConfig.builder().entryNames("[name]").build())
                .withDependencies(getJars(List.of("/mvnpm/stimulus-3.2.1.jar", "/mvnpm/bootstrap-5.2.3.jar")),
                        WebDependencyType.MVNPM)
                .addPlugin(new EsBuildPluginSass())
                .addEntryPoint("app.js").build();

        Bundler.bundle(options, true);

        assertTrue(options.workDir().resolve("dist").resolve("app.js").toFile().exists());
        assertTrue(options.workDir().resolve("dist").resolve("app.css").toFile().exists());
    }

    @Test
    void shouldBundleInDevWithScss() throws IOException, URISyntaxException {
        final Path root = prepareTest();

        final BundleOptions options = BundleOptions.builder()
                .withWorkDir(root)
                .withEsConfig(EsBuildConfig.builder().entryNames("[name]").build())
                .withDependencies(getJars(List.of("/mvnpm/stimulus-3.2.1.jar", "/mvnpm/bootstrap-5.2.3.jar")),
                        WebDependencyType.MVNPM)
                .addPlugin(new EsBuildPluginSass())
                .addEntryPoint("app.js").build();

        try (DevResult dev = Bundler.dev(options, true)) {
            dev.process().build();
        }
        assertTrue(options.workDir().resolve("dist").resolve("app.js").toFile().exists());
        assertTrue(options.workDir().resolve("dist").resolve("app.css").toFile().exists());
    }

    @Test
    void shouldBundleWithScssImports() throws IOException, URISyntaxException {
        final Path root = prepareTest();

        final BundleOptions options = BundleOptions.builder()
                .withWorkDir(root)
                .withEsConfig(EsBuildConfig.builder().entryNames("[name]").build())
                .addPlugin(new EsBuildPluginSass())
                .addEntryPoint("import.scss").build();

        Bundler.bundle(options, true);

        assertTrue(options.workDir().resolve("dist").resolve("import.css").toFile().exists());
    }

    @Test
    void shouldBundleWithScssImportsFilesBeforeFolders() throws IOException, URISyntaxException {
        final Path root = prepareTest();

        final BundleOptions options = BundleOptions.builder()
                .withWorkDir(root)
                .withEsConfig(EsBuildConfig.builder().entryNames("[name]").build())
                .addPlugin(new EsBuildPluginSass())
                .addEntryPoint("partials/test.scss").build();

        Bundler.bundle(options, true);

        assertTrue(options.workDir().resolve("dist").resolve("test.css").toFile().exists());
    }

    static Path prepareTest() throws IOException, URISyntaxException {
        final Path temp = Files.createTempDirectory("test-scss");
        final Path root = new File(BundlerScssTest.class.getResource("/scss/").toURI()).toPath();
        FileUtils.copyDirectory(root.toFile(), temp.toFile());
        return temp;
    }

}
