package io.mvnpm.esbuild.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import io.mvnpm.esbuild.model.AutoEntryPoint.AutoDeps;
import io.mvnpm.esbuild.model.AutoEntryPoint.AutoDepsMode;

public class BundleOptionsBuilder {
    private final BundleOptions options = new BundleOptions();

    public BundleOptionsBuilder() {
        options.setEsBuildConfig(useDefaultConfig());
    }

    private static EsBuildConfig useDefaultConfig() {
        return new EsBuildConfigBuilder().build();
    }

    public BundleOptionsBuilder addAutoEntryPoint(Path sourceDir, String name, List<String> sources) {
        return addEntryPoint(AutoEntryPoint.withoutAutoDeps(sourceDir, name, sources));
    }

    public BundleOptionsBuilder addAutoEntryPoint(Path sourceDir, String name, List<String> sources, AutoDepsMode mode,
            Predicate<String> autoDepsIdsPredicate) {
        return addEntryPoint(AutoEntryPoint.withAutoDeps(sourceDir, name, sources,
                new AutoDeps(mode, options.getNodeModulesDir(), autoDepsIdsPredicate)));
    }

    public BundleOptionsBuilder addEntryPoint(Path rootDir, String script) {
        addEntryPoint(new FileEntryPoint(rootDir, script));
        return this;
    }

    public BundleOptionsBuilder addEntryPoint(String script) {
        if (options.getWorkDir() == null) {
            throw new IllegalArgumentException("Workdir must be set");
        }
        addEntryPoint(new FileEntryPoint(options.getWorkDir(), script));
        return this;
    }

    protected BundleOptionsBuilder addEntryPoint(EntryPoint entry) {
        if (options.getEntries() == null) {
            options.setEntries(new ArrayList<>());
        }
        options.getEntries().add(entry);
        return this;
    }

    public BundleOptionsBuilder withNodeModulesDir(Path nodeModulesDir) {
        this.options.setNodeModulesDir(nodeModulesDir);
        return this;
    }

    public BundleOptionsBuilder withWorkDir(Path workDir) {
        this.options.setWorkDir(workDir);
        return this;
    }

    public BundleOptionsBuilder withDependencies(List<Path> dependencies, WebDependency.WebDependencyType type) {
        this.options.setDependencies(dependencies.stream().map(d -> WebDependency.of(d, type)).toList());
        return this;
    }

    public BundleOptionsBuilder withDependencies(List<WebDependency> dependencies) {
        this.options.setDependencies(dependencies);
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
