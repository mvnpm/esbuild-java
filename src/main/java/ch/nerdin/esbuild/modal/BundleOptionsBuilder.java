package ch.nerdin.esbuild.modal;

import ch.nerdin.esbuild.Bundler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ch.nerdin.esbuild.modal.EsBuildConfig.Loader.FILE;
import static ch.nerdin.esbuild.modal.EsBuildConfig.Loader.JSX;

public class BundleOptionsBuilder {
    private BundleOptions options = new BundleOptions();

    public BundleOptionsBuilder() {
        options.setEsBuildConfig(useDefaultConfig());
    }

    private static EsBuildConfig useDefaultConfig() {
        return new EsBuildConfigBuilder().build();
    }

    public BundleOptionsBuilder addEntryPoint(String name, List<Path> scripts) {
        return addEntryPoint(new BundleEntry(name, scripts));
    }

    public BundleOptionsBuilder addEntryPoint(Path script) {
        addEntryPoint(new FileEntry(script));
        return this;
    }

    protected BundleOptionsBuilder addEntryPoint(Entry entry) {
        if (options.getEntries() == null) {
            options.setEntries(new ArrayList<>());
        }
        options.getEntries().add(entry);
        return this;
    }

    public BundleOptionsBuilder withDependencies(List<Path> dependencies) {
        this.options.setDependencies(dependencies);
        return this;
    }

    public BundleOptionsBuilder withType(Bundler.BundleType type) {
        this.options.setType(type);
        return this;
    }

    public BundleOptionsBuilder withEsConfig(EsBuildConfig esBuildConfig) {
        this.options.setEsBuildConfig(esBuildConfig);
        return this;
    }

    public BundleOptions build() {
        return options;
    }
}
