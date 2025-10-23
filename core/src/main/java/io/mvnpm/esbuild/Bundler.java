package io.mvnpm.esbuild;

import static io.mvnpm.esbuild.script.ScriptRunner.getOutDir;
import static io.mvnpm.esbuild.util.PathUtils.deleteRecursive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import io.mvnpm.esbuild.install.EsBuildDeps;
import io.mvnpm.esbuild.install.WebDepsInstaller;
import io.mvnpm.esbuild.model.*;
import io.mvnpm.esbuild.script.ScriptRunner;

public class Bundler {
    private static final Logger logger = Logger.getLogger(Bundler.class.getName());

    /**
     * Use esbuild to bundle either webjar or mvnpm dependencies into a bundle.
     *
     * @param bundleOptions options to do the bundling with
     * @param install if the dependencies should be installed before bundling
     * @return the folder that has the result of the transformation
     * @throws IOException when something could not be written
     */
    public static BundleResult bundle(BundleOptions bundleOptions, boolean install) throws IOException {
        final Path workDir = getWorkDir(bundleOptions);
        final Path nodeModulesDir = getNodeModulesDir(workDir, bundleOptions);
        if (install) {
            install(nodeModulesDir, bundleOptions.dependencies());
        }
        final Path dist = getOutDir(workDir, bundleOptions.esBuildConfig());
        final BundleOptions effectiveBundleOptions = prepareForBundling(bundleOptions, nodeModulesDir, workDir, dist);
        String output = esBuild(workDir, nodeModulesDir, effectiveBundleOptions);

        if (!Files.isDirectory(dist)) {
            throw new BundleException("Unexpected Error during bundling");
        }

        return new BundleResult(dist, workDir, output);
    }

    public static DevResult dev(BundleOptions bundleOptions, boolean install)
            throws IOException {
        final Path workDir = getWorkDir(bundleOptions);
        final Path nodeModulesDir = getNodeModulesDir(workDir, bundleOptions);
        if (install) {
            install(nodeModulesDir, bundleOptions.dependencies());
        }
        final Path dist = getOutDir(workDir, bundleOptions.esBuildConfig());
        final BundleOptions effectiveBundleOptions = prepareForBundling(bundleOptions, nodeModulesDir, workDir, dist);

        final DevResult devResult = esBuildDev(workDir, nodeModulesDir, effectiveBundleOptions);
        devResult.process().init();
        return devResult;
    }

    private static BundleOptions prepareForBundling(BundleOptions bundleOptions, Path nodeModulesDir, Path workDir, Path dist)
            throws IOException {
        final EsBuildConfig esBuildConfig = bundleOptions.esBuildConfig();
        // Clean the dist directory from a previous bundling
        deleteRecursive(dist);
        Files.createDirectories(dist);

        if (bundleOptions.entries() == null) {
            throw new IllegalArgumentException("At least one entry point is required");
        }
        final List<String> paths = bundleOptions.entries().stream().map(entry -> entry.process(workDir).toString()).toList();
        return bundleOptions.edit()
                .withNodeModulesDir(nodeModulesDir)
                .withEsConfig(esBuildConfig.edit()
                        .outDir(dist.toString())
                        .entryPoint(paths.toArray(String[]::new))
                        .build())
                .build();
    }

    private static Path getWorkDir(BundleOptions bundleOptions) throws IOException {
        return bundleOptions.workDir() != null ? bundleOptions.workDir()
                : Files.createTempDirectory("bundle");
    }

    public static boolean install(Path nodeModulesDir, List<WebDependency> webDeps) throws IOException {
        final List<WebDependency> dependencies = new ArrayList<>(webDeps);
        dependencies.addAll(EsBuildDeps.get().deps());
        return WebDepsInstaller.install(nodeModulesDir, dependencies);
    }

    protected static Path getNodeModulesDir(Path workDir, BundleOptions bundleOptions) {
        return bundleOptions == null || bundleOptions.nodeModulesDir() == null
                ? workDir.resolve(BundleOptions.NODE_MODULES)
                : bundleOptions.nodeModulesDir();
    }

    public static void clearDependencies(Path nodeModulesDir) throws IOException {
        deleteRecursive(nodeModulesDir);
    }

    protected static DevResult esBuildDev(Path workDir, Path nodeModulesDir,
            BundleOptions bundleOptions)
            throws IOException {
        final ScriptRunner scriptRunner = new ScriptRunner(workDir, nodeModulesDir, bundleOptions);
        return new DevResult(scriptRunner.dev());
    }

    protected static String esBuild(Path workDir, Path nodeModulesDir, BundleOptions bundleOptions)
            throws IOException {
        final ScriptRunner scriptRunner = new ScriptRunner(workDir, nodeModulesDir, bundleOptions);
        return scriptRunner.build();
    }

}
