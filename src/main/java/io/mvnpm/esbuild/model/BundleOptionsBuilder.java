package io.mvnpm.esbuild.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import io.mvnpm.esbuild.model.AutoEntryPoint.AutoDeps;
import io.mvnpm.esbuild.model.AutoEntryPoint.AutoDepsMode;

public class BundleOptionsBuilder {

    List<EntryPoint> entries = new ArrayList<>();

    List<WebDependency> dependencies = new ArrayList<>();

    EsBuildConfig esBuildConfig = EsBuildConfig.builder().build();

    Path workDir;

    Path nodeModulesDir;

    BundleOptionsBuilder() {
    }

    public BundleOptionsBuilder addAutoEntryPoint(Path sourceDir, String name, List<String> sources) {
        return addEntryPoint(AutoEntryPoint.withoutAutoDeps(sourceDir, name, sources));
    }

    public BundleOptionsBuilder addAutoEntryPoint(Path sourceDir, String name, List<String> sources, AutoDepsMode mode,
            Predicate<String> autoDepsIdsPredicate) {
        return addEntryPoint(AutoEntryPoint.withAutoDeps(sourceDir, name, sources,
                new AutoDeps(mode, nodeModulesDir, autoDepsIdsPredicate)));
    }

    public BundleOptionsBuilder addEntryPoint(Path rootDir, String script) {
        addEntryPoint(new FileEntryPoint(rootDir, script));
        return this;
    }

    public BundleOptionsBuilder addEntryPoint(String script) {
        if (workDir == null) {
            throw new IllegalArgumentException("Workdir must be set");
        }
        addEntryPoint(new FileEntryPoint(workDir, script));
        return this;
    }

    protected BundleOptionsBuilder addEntryPoint(EntryPoint entry) {
        entries.add(entry);
        return this;
    }

    public BundleOptionsBuilder withNodeModulesDir(Path nodeModulesDir) {
        this.nodeModulesDir = nodeModulesDir;
        return this;
    }

    public BundleOptionsBuilder withWorkDir(Path workDir) {
        this.workDir = workDir;
        return this;
    }

    public BundleOptionsBuilder withDependencies(List<Path> dependencies, WebDependency.WebDependencyType type) {
        this.dependencies = dependencies.stream().map(d -> WebDependency.of(d, type)).toList();
        return this;
    }

    public BundleOptionsBuilder withDependencies(List<WebDependency> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public BundleOptionsBuilder withEsConfig(EsBuildConfig esBuildConfig) {
        this.esBuildConfig = esBuildConfig;
        return this;
    }

    public BundleOptions build() {
        return new BundleOptions(this);
    }

}
