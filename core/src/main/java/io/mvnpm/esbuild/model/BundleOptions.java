package io.mvnpm.esbuild.model;

import java.nio.file.Path;
import java.util.List;

public record BundleOptions(List<EntryPoint> entries, List<WebDependency> dependencies, EsBuildConfig esBuildConfig,
        List<EsBuildPlugin> plugins,
        Path workDir, Path nodeModulesDir) {

    public static final String NODE_MODULES = "node_modules";

    public BundleOptions(BundleOptionsBuilder builder) {
        this(builder.entries, builder.dependencies, builder.esBuildConfig, builder.plugins, builder.workDir,
                builder.nodeModulesDir);
    }

    public boolean hasWorkDir() {
        return workDir == null;
    }

    public static BundleOptionsBuilder builder() {
        return new BundleOptionsBuilder();
    }

    public BundleOptionsBuilder edit() {
        return builder()
                .withEntries(entries)
                .withDependencies(dependencies)
                .withEsConfig(esBuildConfig)
                .withPlugins(plugins)
                .withWorkDir(workDir)
                .withNodeModulesDir(nodeModulesDir);

    }

}
