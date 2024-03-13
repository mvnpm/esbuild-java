package io.mvnpm.esbuild.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BundleOptions {

    public static final String NODE_MODULES = "node_modules";

    private List<EntryPoint> entries;

    private List<WebDependency> dependencies = new ArrayList<>();

    private EsBuildConfig esBuildConfig;

    private Path workDir;

    private Path nodeModulesDir;

    public List<EntryPoint> getEntries() {
        return entries;
    }

    public void setEntries(List<EntryPoint> entries) {
        this.entries = entries;
    }

    public List<WebDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<WebDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public EsBuildConfig getEsBuildConfig() {
        return esBuildConfig;
    }

    public void setEsBuildConfig(EsBuildConfig esBuildConfig) {
        this.esBuildConfig = esBuildConfig;
    }

    public Path getWorkDir() {
        return workDir;
    }

    public boolean hasWorkDir() {
        return workDir == null;
    }

    public void setWorkDir(Path workDir) {
        this.workDir = workDir;
    }

    public Path getNodeModulesDir() {
        return nodeModulesDir;
    }

    public BundleOptions setNodeModulesDir(Path nodeModulesDir) {
        this.nodeModulesDir = nodeModulesDir;
        return this;
    }

}
