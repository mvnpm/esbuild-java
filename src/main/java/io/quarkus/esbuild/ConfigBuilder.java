package io.quarkus.esbuild;

import java.util.List;
import java.util.Map;

public class ConfigBuilder {
    private final Config config;

    public ConfigBuilder() {
        this.config = new Config();
    }

    public ConfigBuilder bundle(boolean bundle) {
        config.setBundle(bundle);
        return this;
    }

    public ConfigBuilder entryPoint(String entryPoint) {
        config.setEntryPoint(entryPoint);
        return this;
    }

    public ConfigBuilder minify(boolean minify) {
        config.setMinify(minify);
        return this;
    }

    public ConfigBuilder version(boolean version) {
        config.setVersion(version);
        return this;
    }

    public ConfigBuilder substitutes(Map<String, String> substitutes) {
        config.setSubstitutes(substitutes);
        return this;
    }

    public ConfigBuilder excludes(List<String> excludes) {
        config.setExcludes(excludes);
        return this;
    }

    public ConfigBuilder format(Config.Format format) {
        config.setFormat(format);
        return this;
    }

    public ConfigBuilder loader(Map<String, Config.Loader> loader) {
        config.setLoader(loader);
        return this;
    }

    public ConfigBuilder outDir(String outDir) {
        config.setOutDir(outDir);
        return this;
    }

    public ConfigBuilder packages(String packages) {
        config.setPackages(packages);
        return this;
    }

    public ConfigBuilder platform(Config.Platform platform) {
        config.setPlatform(platform);
        return this;
    }

    public ConfigBuilder serve(String serve) {
        config.setServe(serve);
        return this;
    }

    public ConfigBuilder sourceMap(boolean sourceMap) {
        config.setSourceMap(sourceMap);
        return this;
    }

    public ConfigBuilder target(Config.Target target) {
        config.setTarget(target);
        return this;
    }

    public ConfigBuilder watch(boolean watch) {
        config.setWatch(watch);
        return this;
    }

    public Config build() {
        return config;
    }
}
