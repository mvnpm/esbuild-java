package ch.nerdin.esbuild;

import java.util.List;
import java.util.Map;

public class ConfigBuilder {
    private final Config config;

    public ConfigBuilder() {
        this.config = new Config();
    }

    public ConfigBuilder bundle() {
        config.setBundle(true);
        return this;
    }

    public ConfigBuilder entryPoint(String entryPoint) {
        config.setEntryPoint(entryPoint);
        return this;
    }

    public ConfigBuilder minify() {
        config.setMinify(true);
        return this;
    }

    public ConfigBuilder version() {
        config.setVersion(true);
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

    public ConfigBuilder serve() {
        config.setServe(true);
        return this;
    }

    public ConfigBuilder sourceMap() {
        config.setSourceMap(true);
        return this;
    }

    public ConfigBuilder splitting() {
        config.setSplitting(true);
        return this;
    }

    public ConfigBuilder target(Config.Target target) {
        config.setTarget(target);
        return this;
    }

    public ConfigBuilder watch() {
        config.setWatch(true);
        return this;
    }

    public Config build() {
        return config;
    }
}
