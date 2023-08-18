package ch.nerdin.esbuild.model;

import ch.nerdin.esbuild.Bundler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BundleOptions {

    private List<EntryPoint> entries;

    private List<Path> dependencies = new ArrayList<>();

    private Bundler.BundleType type;

    private EsBuildConfig esBuildConfig;

    private Path workDir;

    public List<EntryPoint> getEntries() {
        return entries;
    }

    public void setEntries(List<EntryPoint> entries) {
        this.entries = entries;
    }

    public List<Path> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Path> dependencies) {
        this.dependencies = dependencies;
    }

    public Bundler.BundleType getType() {
        return type;
    }

    public void setType(Bundler.BundleType type) {
        this.type = type;
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
}
