package ch.nerdin.esbuild;

import java.nio.file.Path;
import java.util.List;

public class BundleOptions {
    private String bundleName;

    private List<Path> dependencies;

    private Bundler.BundleType type;

    private List<Path> entries;

    private EsBuildConfig esBuildConfig;

    public String getBundleName() {
        return bundleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
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

    public List<Path> getEntries() {
        return entries;
    }

    public void setEntries(List<Path> entries) {
        this.entries = entries;
    }

    public EsBuildConfig getEsBuildConfig() {
        return esBuildConfig;
    }

    public void setEsBuildConfig(EsBuildConfig esBuildConfig) {
        this.esBuildConfig = esBuildConfig;
    }
}
