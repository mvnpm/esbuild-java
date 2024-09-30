package io.mvnpm.esbuild.model;

import static io.mvnpm.esbuild.model.EsBuildConfig.Loader.CSS;
import static io.mvnpm.esbuild.model.EsBuildConfig.Loader.FILE;
import static io.mvnpm.esbuild.model.EsBuildConfig.Loader.JS;
import static io.mvnpm.esbuild.model.EsBuildConfig.Loader.JSON;
import static io.mvnpm.esbuild.model.EsBuildConfig.Loader.JSX;
import static io.mvnpm.esbuild.model.EsBuildConfig.Loader.TS;
import static io.mvnpm.esbuild.model.EsBuildConfig.Loader.TSX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EsBuildConfigBuilder {

    String esBuildVersion;
    boolean bundle;

    String[] entryPoint;
    boolean minify;

    boolean version;

    Map<String, EsBuildConfig.Loader> loader;

    boolean preserveSymlinks;

    EsBuildConfig.Target target;

    boolean watch;

    String outdir;
    String packages;

    EsBuildConfig.Platform platform;

    boolean serve;
    boolean sourceMap;

    boolean splitting;

    final Map<String, String> alias = new HashMap<>();
    final Map<String, String> define = new HashMap<>();

    List<String> excludes;

    EsBuildConfig.Format format;

    String chunkNames;

    String entryNames;

    String assetNames;

    String publicPath;

    List<String> external = new ArrayList<>();

    EsBuildConfigBuilder() {
    }

    public EsBuildConfigBuilder withDefault() {
        return this.bundle()
                .minify()
                .sourceMap()
                .splitting()
                .hashedNames()
                .format(EsBuildConfig.Format.ESM)
                .loader(getDefaultLoadersMap());
    }

    public EsBuildConfigBuilder hashedNames() {
        return this.entryNames("[name]-[hash]")
                .assetNames("assets/[name]-[hash]");
    }

    public EsBuildConfigBuilder fixedEntryNames() {
        return this.entryNames("[name]");
    }

    public static Map<String, EsBuildConfig.Loader> getDefaultLoadersMap() {
        Map<String, EsBuildConfig.Loader> loaders = new HashMap<>();
        loaders.put(".css", CSS);
        loaders.put(".json", JSON);
        loaders.put(".jsx", JSX);
        loaders.put(".tsx", TSX);
        loaders.put(".ts", TS);
        loaders.put(".js", JS);
        loaders.put(".svg", FILE);
        loaders.put(".gif", FILE);
        loaders.put(".png", FILE);
        loaders.put(".jpg", FILE);
        loaders.put(".woff", FILE);
        loaders.put(".woff2", FILE);
        loaders.put(".ttf", FILE);
        loaders.put(".eot", FILE);
        return loaders;
    }

    public EsBuildConfigBuilder esbuildVersion(String esbuildVersion) {
        this.esBuildVersion = esbuildVersion;
        return this;
    }

    public EsBuildConfigBuilder bundle() {
        this.bundle = true;
        return this;
    }

    public EsBuildConfigBuilder bundle(boolean bundle) {
        this.bundle = bundle;
        return this;
    }

    public EsBuildConfigBuilder entryPoint(String[] entryPoint) {
        this.entryPoint = entryPoint;
        return this;
    }

    public EsBuildConfigBuilder minify() {
        this.minify = true;
        return this;
    }

    public EsBuildConfigBuilder minify(boolean minify) {
        this.minify = minify;
        return this;
    }

    public EsBuildConfigBuilder version() {
        this.version = true;
        return this;
    }

    public EsBuildConfigBuilder version(boolean version) {
        this.version = version;
        return this;
    }

    public EsBuildConfigBuilder alias(Map<String, String> alias) {
        this.alias.putAll(alias);
        return this;
    }

    public EsBuildConfigBuilder define(Map<String, String> define) {
        this.define.putAll(define);
        return this;
    }

    public EsBuildConfigBuilder alias(String key, String value) {
        this.define.put(key, value);
        return this;
    }

    public EsBuildConfigBuilder define(String key, String value) {
        this.define.put(key, value);
        return this;
    }

    public EsBuildConfigBuilder excludes(List<String> excludes) {
        this.excludes = excludes;
        return this;
    }

    public EsBuildConfigBuilder format(EsBuildConfig.Format format) {
        this.format = format;
        return this;
    }

    public EsBuildConfigBuilder loader(Map<String, EsBuildConfig.Loader> loader) {
        this.loader = loader;
        return this;
    }

    public EsBuildConfigBuilder preserveSymlinks() {
        this.preserveSymlinks = true;
        return this;
    }

    public EsBuildConfigBuilder preserveSymlinks(boolean preserveSymlinks) {
        this.preserveSymlinks = preserveSymlinks;
        return this;
    }

    public EsBuildConfigBuilder outDir(String outDir) {
        this.outdir = outDir;
        return this;
    }

    public EsBuildConfigBuilder packages(String packages) {
        this.packages = packages;
        return this;
    }

    public EsBuildConfigBuilder platform(EsBuildConfig.Platform platform) {
        this.platform = platform;
        return this;
    }

    public EsBuildConfigBuilder serve() {
        this.serve = true;
        return this;
    }

    public EsBuildConfigBuilder serve(boolean serve) {
        this.serve = serve;
        return this;
    }

    public EsBuildConfigBuilder sourceMap() {
        this.sourceMap = true;
        return this;
    }

    public EsBuildConfigBuilder sourceMap(boolean sourceMap) {
        this.sourceMap = sourceMap;
        return this;
    }

    public EsBuildConfigBuilder splitting() {
        this.splitting = true;
        return this;
    }

    public EsBuildConfigBuilder splitting(boolean splitting) {
        this.splitting = splitting;
        return this;
    }

    public EsBuildConfigBuilder addExternal(String name) {
        this.external.add(name);
        return this;
    }

    public EsBuildConfigBuilder external(List<String> names) {
        this.external = names;
        return this;
    }

    public EsBuildConfigBuilder target(EsBuildConfig.Target target) {
        this.target = target;
        return this;
    }

    public EsBuildConfigBuilder watch() {
        this.watch = true;
        return this;
    }

    public EsBuildConfigBuilder watch(boolean watch) {
        this.watch = watch;
        return this;
    }

    public EsBuildConfigBuilder chunkNames(String template) {
        this.chunkNames = template;
        return this;
    }

    public EsBuildConfigBuilder entryNames(String template) {
        this.entryNames = template;
        return this;
    }

    public EsBuildConfigBuilder publicPath(String publicPath) {
        this.publicPath = publicPath;
        return this;
    }

    public EsBuildConfigBuilder assetNames(String assetNames) {
        this.assetNames = assetNames;
        return this;
    }

    public EsBuildConfig build() {
        return new EsBuildConfig(this);
    }
}
