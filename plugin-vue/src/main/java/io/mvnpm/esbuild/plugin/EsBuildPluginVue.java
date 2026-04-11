package io.mvnpm.esbuild.plugin;

import io.mvnpm.esbuild.model.EsBuildPlugin;

public class EsBuildPluginVue implements EsBuildPlugin {

    @Override
    public String name() {

        return "vue";
    }

    @Override
    public String importScript() {

        return """
                import vuePlugin from "esbuild-plugin-vue3";
                """;
    }

    @Override
    public String configurePlugin() {

        // language=JavaScript
        return """
                (function (config, data) {
                    config.plugins.push(vuePlugin());
                    return config;
                })
                """;
    }
}
