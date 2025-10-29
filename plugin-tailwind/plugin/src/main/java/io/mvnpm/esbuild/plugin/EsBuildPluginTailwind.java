package io.mvnpm.esbuild.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import io.mvnpm.esbuild.model.EsBuildPlugin;

public record EsBuildPluginTailwind() implements EsBuildPlugin {
    @Override
    public String name() {
        return "tailwind";
    }

    @Override
    public void beforeBuild(Path workDir) {
        try (InputStream resourceAsStream = EsBuildPluginTailwind.class.getResourceAsStream("/esbuild-plugin-tailwind.js")) {
            Files.copy(resourceAsStream, workDir.resolve("esbuild-plugin-tailwind.js"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String importScript() {
        return "import esbuildPluginTailwind from './esbuild-plugin-tailwind.js';";
    }

    @Override
    public String buildConfigMapper() {
        return "(config) => { config.plugins.push(esbuildPluginTailwind()); return config; }";
    }
}
