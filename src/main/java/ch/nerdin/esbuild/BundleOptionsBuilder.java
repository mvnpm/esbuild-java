package ch.nerdin.esbuild;

import java.nio.file.Path;
import java.util.List;

public class BundleOptionsBuilder {
    private String bundleName;
    private List<Path> dependencies;
    private Bundler.BundleType type;
    private List<Path> entries;

    private EsBuildConfig esBuildConfig;

    public BundleOptionsBuilder() {
        this.esBuildConfig = useDefaultConfig();
        this.bundleName = "bundle";
    }

    private static EsBuildConfig useDefaultConfig() {
        return new EsBuildConfigBuilder().bundle().minify().sourceMap().splitting().format(EsBuildConfig.Format.ESM).build();
    }

    public BundleOptionsBuilder withBundleName(String bundleName) {
        this.bundleName = bundleName;
        return this;
    }

    public BundleOptionsBuilder withDependencies(List<Path> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public BundleOptionsBuilder withType(Bundler.BundleType type) {
        this.type = type;
        return this;
    }

    public BundleOptionsBuilder withEntries(List<Path> entries) {
        this.entries = entries;
        return this;
    }

    public BundleOptionsBuilder withEntry(Path entry) {
        this.entries = List.of(entry);
        return this;
    }

    public BundleOptions build() {
        BundleOptions options = new BundleOptions();
        options.setBundleName(bundleName);
        options.setDependencies(dependencies);
        options.setType(type);
        options.setEntries(entries);
        options.setEsBuildConfig(esBuildConfig);
        return options;
    }
}
