package io.mvnpm.esbuild.model;

import java.nio.file.Files;
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

    List<EsBuildPlugin> plugins = new ArrayList<>();

    long timeoutSeconds = 60;

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

    public BundleOptionsBuilder addEntryPoint(Path script) {
        if (!Files.exists(script)) {
            throw new IllegalArgumentException(String.format("script %s does not exist", script));
        }
        addEntryPoint(new FileEntryPoint(script.toAbsolutePath().getParent(), script.getFileName().toString()));
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

    protected BundleOptionsBuilder withEntries(List<EntryPoint> entries) {
        this.entries = entries;
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

    public BundleOptionsBuilder withPlugins(List<EsBuildPlugin> plugins) {
        this.plugins = plugins;
        return this;
    }

    public BundleOptionsBuilder addPlugin(EsBuildPlugin plugin) {
        this.plugins.add(plugin);
        return this;
    }

    public BundleOptionsBuilder withTimeout(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    public BundleOptions build() {
        return new BundleOptions(this);
    }

}
