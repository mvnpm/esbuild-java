package ch.nerdin.esbuild.modal;

import java.util.List;
import java.util.Map;

public class EsBuildConfigBuilder {
    private final EsBuildConfig esBuildConfig;

    public EsBuildConfigBuilder() {
        this.esBuildConfig = new EsBuildConfig();
    }

    public EsBuildConfigBuilder bundle() {
        esBuildConfig.setBundle(true);
        return this;
    }

    public EsBuildConfigBuilder entryPoint(String[] entryPoint) {
        esBuildConfig.setEntryPoint(entryPoint);
        return this;
    }

    public EsBuildConfigBuilder minify() {
        esBuildConfig.setMinify(true);
        return this;
    }

    public EsBuildConfigBuilder version() {
        esBuildConfig.setVersion(true);
        return this;
    }

    public EsBuildConfigBuilder substitutes(Map<String, String> substitutes) {
        esBuildConfig.setSubstitutes(substitutes);
        return this;
    }

    public EsBuildConfigBuilder excludes(List<String> excludes) {
        esBuildConfig.setExcludes(excludes);
        return this;
    }

    public EsBuildConfigBuilder format(EsBuildConfig.Format format) {
        esBuildConfig.setFormat(format);
        return this;
    }

    public EsBuildConfigBuilder loader(Map<String, EsBuildConfig.Loader> loader) {
        esBuildConfig.setLoader(loader);
        return this;
    }

    public EsBuildConfigBuilder outDir(String outDir) {
        esBuildConfig.setOutDir(outDir);
        return this;
    }

    public EsBuildConfigBuilder packages(String packages) {
        esBuildConfig.setPackages(packages);
        return this;
    }

    public EsBuildConfigBuilder platform(EsBuildConfig.Platform platform) {
        esBuildConfig.setPlatform(platform);
        return this;
    }

    public EsBuildConfigBuilder serve() {
        esBuildConfig.setServe(true);
        return this;
    }

    public EsBuildConfigBuilder sourceMap() {
        esBuildConfig.setSourceMap(true);
        return this;
    }

    public EsBuildConfigBuilder splitting() {
        esBuildConfig.setSplitting(true);
        return this;
    }

    public EsBuildConfigBuilder target(EsBuildConfig.Target target) {
        esBuildConfig.setTarget(target);
        return this;
    }

    public EsBuildConfigBuilder watch() {
        esBuildConfig.setWatch(true);
        return this;
    }

    public EsBuildConfig build() {
        return esBuildConfig;
    }
}
