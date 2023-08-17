package ch.nerdin.esbuild.model;

import ch.nerdin.esbuild.Bundler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class BundleOptionsBuilder {
    private final BundleOptions options = new BundleOptions();

    public BundleOptionsBuilder() {
        options.setEsBuildConfig(useDefaultConfig());
    }

    private static EsBuildConfig useDefaultConfig() {
        return new EsBuildConfigBuilder().build();
    }

    public BundleOptionsBuilder addAutoEntryPoint(Path sourceDir, String name, List<String> scripts) {
        return addEntryPoint(new AutoEntryPoint(sourceDir, name, scripts));
    }

    public BundleOptionsBuilder addEntryPoint(Path rootDir, String script) {
        addEntryPoint(new FileEntryPoint(rootDir, script));
        return this;
    }

    protected BundleOptionsBuilder addEntryPoint(EntryPoint entry) {
        if (options.getEntries() == null) {
            options.setEntries(new ArrayList<>());
        }
        options.getEntries().add(entry);
        return this;
    }

    public BundleOptionsBuilder setWorkFolder(Path workFolder) {
        this.options.setWorkDir(workFolder);
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
