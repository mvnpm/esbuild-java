package io.mvnpm.esbuild;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import io.mvnpm.esbuild.model.BundleOptions;
import io.mvnpm.esbuild.model.DevResult;
import io.mvnpm.esbuild.model.EsBuildConfig;
import io.mvnpm.esbuild.plugin.EsBuildPluginSvelte;

public class BundlerSvelteTest {

    @Test
    void shouldBundleWithSvelte() throws IOException, URISyntaxException {

        final Path root = prepareTest();

        final BundleOptions options = BundleOptions.builder()
                .withWorkDir(root)
                .withEsConfig(EsBuildConfig.builder().entryNames("[name]").build())
                .addPlugin(new EsBuildPluginSvelte(false))
                .addEntryPoint("app.js").build();

        Bundler.bundle(options, true);

        assertTrue(options.workDir().resolve("dist").resolve("app.js").toFile().exists());
        assertTrue(options.workDir().resolve("dist").resolve("app.css").toFile().exists());
    }

    @Test
    void shouldBundleInDevWithSvelte() throws IOException, URISyntaxException {
        final Path root = prepareTest();

        final BundleOptions options = BundleOptions.builder()
                .withWorkDir(root)
                .withEsConfig(EsBuildConfig.builder().entryNames("[name]").build())
                .addPlugin(new EsBuildPluginSvelte(false))
                .addEntryPoint("app.js").build();

        try (DevResult dev = Bundler.dev(options, true)) {
            dev.process().build();
        }
        assertTrue(options.workDir().resolve("dist").resolve("app.js").toFile().exists());
        assertTrue(options.workDir().resolve("dist").resolve("app.css").toFile().exists());
    }

    @Test
    void shouldCreateWebComponentWithSvelte() throws IOException, URISyntaxException {

        final Path root = prepareTest();

        final BundleOptions options = BundleOptions.builder()
                .withWorkDir(root)
                .withEsConfig(EsBuildConfig.builder().entryNames("[name]").build())
                .addPlugin(new EsBuildPluginSvelte(true))
                .addEntryPoint("app-customElement.js").build();

        Bundler.bundle(options, true);

        var content = Files.readString(options.workDir().resolve("dist").resolve("app-customElement.js"));
        assertTrue(content.contains("customElements.define(\"my-element\""));
    }

    static Path prepareTest() throws IOException, URISyntaxException {
        final Path temp = Files.createTempDirectory("test-svelte");
        final Path root = new File(BundlerSvelteTest.class.getResource("/svelte/").toURI()).toPath();
        FileUtils.copyDirectory(root.toFile(), temp.toFile());
        return temp;
    }

}
