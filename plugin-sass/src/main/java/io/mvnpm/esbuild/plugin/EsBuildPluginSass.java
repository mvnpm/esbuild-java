package io.mvnpm.esbuild.plugin;

import io.mvnpm.esbuild.model.EsBuildPlugin;

public record EsBuildPluginSass() implements EsBuildPlugin {
    @Override
    public String name() {
        return "sass";
    }

    @Override
    public String importScript() {
        return "import {sassPlugin} from 'esbuild-sass-plugin';";
    }

    @Override
    public String requireScript() {
        return "sassPlugin()";
    }
}
