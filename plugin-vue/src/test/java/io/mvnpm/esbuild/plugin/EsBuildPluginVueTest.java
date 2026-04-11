package io.mvnpm.esbuild.plugin;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import io.mvnpm.esbuild.Bundler;
import io.mvnpm.esbuild.model.BundleOptions;
import io.mvnpm.esbuild.model.DevResult;
import io.mvnpm.esbuild.model.EsBuildConfig;

public class EsBuildPluginVueTest {

    @Test
    void shouldBundleWithVue() throws Exception {

        // given
        final Path root = prepareTest();
        final BundleOptions options = BundleOptions.builder().withWorkDir(root)
                .withEsConfig(EsBuildConfig.builder().entryNames("[name]").build()).addPlugin(new EsBuildPluginVue())
                .addEntryPoint("app.js").build();

        // when
        Bundler.bundle(options, true);

        // then
        assertTrue(options.workDir().resolve("dist").resolve("app.js").toFile().exists());
        assertTrue(options.workDir().resolve("dist").resolve("app.css").toFile().exists());
    }

    @Test
    void shouldBundleInDevWithVue() throws Exception {

        // given
        final Path root = prepareTest();
        final BundleOptions options = BundleOptions.builder().withWorkDir(root)
                .withEsConfig(EsBuildConfig.builder().entryNames("[name]").build()).addPlugin(new EsBuildPluginVue())
                .addEntryPoint("app.js").build();

        // when
        try (DevResult dev = Bundler.dev(options, true)) {

            dev.process().build();
        }

        // then
        assertTrue(options.workDir().resolve("dist").resolve("app.js").toFile().exists());
        assertTrue(options.workDir().resolve("dist").resolve("app.css").toFile().exists());
    }

    static Path prepareTest() throws IOException, URISyntaxException {

        final Path temp = Files.createTempDirectory("test-vue");
        final Path root = new File(EsBuildPluginVueTest.class.getResource("/vue/").toURI()).toPath();
        FileUtils.copyDirectory(root.toFile(), temp.toFile());
        return temp;
    }
}
