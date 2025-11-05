package io.mvnpm.esbuild;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import io.mvnpm.esbuild.model.BundleOptions;
import io.mvnpm.esbuild.model.BundleResult;
import io.mvnpm.esbuild.model.DevResult;
import io.mvnpm.esbuild.model.EsBuildConfig;
import io.mvnpm.esbuild.plugin.EsBuildPluginTailwind;

public class BundlerTailwindTest {

    @Test
    void shouldBundle() throws IOException, URISyntaxException {

        final Path root = prepareTest();

        final BundleOptions options = BundleOptions.builder()
                .withWorkDir(root)
                .withEsConfig(EsBuildConfig.builder().fixedEntryNames().build())
                .addPlugin(new EsBuildPluginTailwind())
                .addEntryPoint("app.js").build();

        final BundleResult result = Bundler.bundle(options, true);
        assertTrue(options.workDir().resolve("dist").resolve("app.js").toFile().exists());
        assertTrue(options.workDir().resolve("dist").resolve("app.css").toFile().exists());
    }

    @Test
    void shouldBundleInDev() throws IOException, URISyntaxException {
        final Path root = prepareTest();

        final BundleOptions options = BundleOptions.builder()
                .withWorkDir(root)
                .withEsConfig(EsBuildConfig.builder().entryNames("[name]").build())
                .addPlugin(new EsBuildPluginTailwind())
                .addEntryPoint("app.js").build();

        try (DevResult dev = Bundler.dev(options, true)) {
            dev.process().build();

        }
        assertTrue(options.workDir().resolve("dist").resolve("app.js").toFile().exists());
        assertTrue(options.workDir().resolve("dist").resolve("app.css").toFile().exists());
    }

    static Path prepareTest() throws IOException, URISyntaxException {
        final Path temp = Files.createTempDirectory("test-tailwind");
        final Path root = new File(BundlerTailwindTest.class.getResource("/tailwind/").toURI()).toPath();
        FileUtils.copyDirectory(root.toFile(), temp.toFile());
        return temp;
    }

}
