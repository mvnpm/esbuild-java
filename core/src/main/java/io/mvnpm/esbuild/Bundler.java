package io.mvnpm.esbuild;

import static io.mvnpm.esbuild.script.ScriptRunner.getOutDir;
import static io.mvnpm.esbuild.util.PathUtils.deleteRecursive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

import io.mvnpm.esbuild.deno.ScriptLog;
import io.mvnpm.esbuild.install.EsBuildDeps;
import io.mvnpm.esbuild.install.WebDepsInstaller;
import io.mvnpm.esbuild.model.*;
import io.mvnpm.esbuild.script.ScriptRunner;

public class Bundler {
    private static final Logger LOG = Logger.getLogger(Bundler.class);

    /**
     * Use esbuild to bundle either webjar or mvnpm dependencies into a bundle.
     *
     * @param bundleOptions options to do the bundling with
     * @param install if the dependencies should be installed before bundling
     * @return the folder that has the result of the transformation
     * @throws IOException when something could not be written
     */
    public static BundleResult bundle(BundleOptions bundleOptions, boolean install) throws IOException {
        final Bundling bundling = getBundling(bundleOptions, install);
        ScriptLog log = esBuild(bundling.workDir(), bundling.nodeModulesDir(), bundling.bundleOptions());

        if (!Files.isDirectory(bundling.dist())) {
            throw new BundlingException("Unexpected Error during bundling", log);
        }

        log.logAll();

        return new BundleResult(bundling.dist(), bundling.workDir(), log);
    }

    public static DevResult dev(BundleOptions bundleOptions, boolean install)
            throws IOException {
        final Bundling bundling = getBundling(bundleOptions, install);
        final DevResult devResult = esBuildDev(bundling.workDir(), bundling.nodeModulesDir(), bundling.bundleOptions());
        devResult.process().init();
        return devResult;
    }

    private static Bundling getBundling(BundleOptions bundleOptions, boolean install) throws IOException {
        final Path workDir = getWorkDir(bundleOptions);
        final Path nodeModulesDir = getNodeModulesDir(workDir, bundleOptions);

        if (nodeModulesDir.getParent() == null
                || !workDir.toAbsolutePath().startsWith(nodeModulesDir.getParent().toAbsolutePath())) {
            throw new BundlingException(
                    "Invalid node_modules directory: '%s'. It must be located in an ancestor of the working directory '%s' to enable module resolution."
                            .formatted(nodeModulesDir, workDir));
        }

        if (install) {
            install(nodeModulesDir, bundleOptions.dependencies());
        }
        final Path dist = getOutDir(workDir, bundleOptions.esBuildConfig());
        final BundleOptions effectiveBundleOptions = prepareForBundling(bundleOptions, nodeModulesDir, workDir, dist);
        return new Bundling(workDir, nodeModulesDir, dist, effectiveBundleOptions);
    }

    private record Bundling(Path workDir, Path nodeModulesDir, Path dist, BundleOptions bundleOptions) {
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
        return bundleOptions.workDir() != null ? bundleOptions.workDir().normalize()
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
                : bundleOptions.nodeModulesDir().normalize();
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

    protected static ScriptLog esBuild(Path workDir, Path nodeModulesDir, BundleOptions bundleOptions)
            throws IOException {
        final ScriptRunner scriptRunner = new ScriptRunner(workDir, nodeModulesDir, bundleOptions);
        return scriptRunner.build();
    }

}
