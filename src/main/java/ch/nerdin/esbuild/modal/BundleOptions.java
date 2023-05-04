package ch.nerdin.esbuild.modal;

import ch.nerdin.esbuild.Bundler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BundleOptions {

    private List<Entry> entries;

    private List<Path> dependencies = new ArrayList<>();

    private Bundler.BundleType type;

    private EsBuildConfig esBuildConfig;

    private Path root;

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
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

    public Path getRoot() {
        return root;
    }

    public void setRoot(Path root) {
        this.root = root;
    }
}
