package io.mvnpm.esbuild.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.mvnpm.esbuild.model.EsBuildPlugin;

public record EsBuildPluginTailwind(String basePath) implements EsBuildPlugin {

    public EsBuildPluginTailwind() {
        this(null);
    }

    @Override
    public String name() {
        return "tailwind";
    }

    @Override
    public void beforeBuild(Path workDir) {
        try (InputStream tailwindPlugin = EsBuildPluginTailwind.class.getResourceAsStream("/esbuild-plugin-tailwind.js")) {
            Objects.requireNonNull(tailwindPlugin, "tailwindPlugin is required");
            Files.copy(tailwindPlugin, workDir.resolve("esbuild-plugin-tailwind.js"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object data() {
        final var data = new HashMap<String, String>();
        data.put("base", basePath);
        return data;
    }

    @Override
    public String importScript() {
        return "import esbuildPluginTailwind from './esbuild-plugin-tailwind.js';";
    }

    @Override
    public String configurePlugin() {
        // language=JavaScript
        return """
                (function(config, data) { config.plugins.push(esbuildPluginTailwind(data)); return config; })
                """;
    }
}
