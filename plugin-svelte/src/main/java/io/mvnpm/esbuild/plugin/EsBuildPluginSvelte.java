package io.mvnpm.esbuild.plugin;

import java.util.HashMap;

import io.mvnpm.esbuild.model.EsBuildPlugin;

public record EsBuildPluginSvelte(
        boolean customElement
) implements EsBuildPlugin {
    @Override
    public String name() {
        return "svelte";
    }

    @Override
    public String importScript() {
        return """
                import sveltePlugin from 'esbuild-svelte';
                """;
    }

    @Override
    public Object data() {
        final var data = new HashMap<String, Object>();
        data.put("customElement", customElement);
        return data;
    }

    @Override
    public String configurePlugin() {
        // language=JavaScript
        return """
                (function (config, data) {
                    config.plugins.push(sveltePlugin({
                        compilerOptions: {
                            customElement: data.customElement
                        }
                    }));
                    return config;
                })
                """;
    }
}
