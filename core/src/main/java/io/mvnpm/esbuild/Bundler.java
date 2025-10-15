package io.mvnpm.esbuild;

import static io.mvnpm.esbuild.script.ScriptRunner.getOutDir;
import static io.mvnpm.esbuild.util.PathUtils.deleteRecursive;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import io.mvnpm.esbuild.install.EsBuildDeps;
import io.mvnpm.esbuild.install.WebDepsInstaller;
import io.mvnpm.esbuild.model.*;
import io.mvnpm.esbuild.script.ScriptRunner;

public class Bundler {
    private static final Logger logger = Logger.getLogger(Bundler.class.getName());

    public static final String ESBUILD_EMBEDDED_VERSION = resolveEmbeddedVersion();

    private static String resolveEmbeddedVersion() {
        Properties properties = new Properties();
        try {
            final InputStream resource = Bundler.class.getResourceAsStream("/esbuild-java-version.properties");
            if (resource != null) {
                properties.load(resource);
            }
        } catch (IOException e) {
            // ignore we use the default
        }
        String version = properties.getProperty("esbuild.version");
        return requireNonNull(version, "Make sure the esbuild-java-version.properties contains 'esbuild.version'.");
    }

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
        if (install) {
            install(workDir, bundleOptions);
        }
        final Path dist = getOutDir(workDir, bundleOptions.esBuildConfig());
        final EsBuildConfig esBuildConfig = prepareForBundling(bundleOptions, workDir, dist, false);

        esBuild(workDir, esBuildConfig);

        if (!Files.isDirectory(dist)) {
            throw new BundleException("Unexpected Error during bundling");
        }

        return new BundleResult(dist, workDir);
    }

    public static DevResult dev(BundleOptions bundleOptions, boolean install)
            throws IOException {
        final Path workDir = getWorkDir(bundleOptions);
        if (install) {
            install(workDir, bundleOptions);
        }
        final Path dist = getOutDir(workDir, bundleOptions.esBuildConfig());
        final EsBuildConfig esBuildConfig = prepareForBundling(bundleOptions, workDir, dist, true);

        final DevResult devResult = esBuildDev(workDir, esBuildConfig);
        devResult.process().init();
        return devResult;
    }

    private static EsBuildConfig prepareForBundling(BundleOptions bundleOptions, Path workDir, Path dist, boolean watch)
            throws IOException {
        final EsBuildConfig esBuildConfig = bundleOptions.esBuildConfig();
        // Clean the dist directory from a previous bundling
        deleteRecursive(dist);
        Files.createDirectories(dist);

        if (bundleOptions.entries() == null) {
            throw new IllegalArgumentException("At least one entry point is required");
        }
        final List<String> paths = bundleOptions.entries().stream().map(entry -> entry.process(workDir).toString()).toList();
        return esBuildConfig.edit()
                .outDir(dist.toString())
                .watch(watch)
                .entryPoint(paths.toArray(String[]::new))
                .build();
    }

    private static Path getWorkDir(BundleOptions bundleOptions) throws IOException {
        return bundleOptions.workDir() != null ? bundleOptions.workDir()
                : Files.createTempDirectory("bundle");
    }

    public static boolean install(Path workDir, BundleOptions bundleOptions) throws IOException {
        final Path nodeModulesDir = getNodeModulesDir(workDir, bundleOptions);
        final List<WebDependency> dependencies = new ArrayList<>(bundleOptions.dependencies());
        dependencies.addAll(EsBuildDeps.get().deps());
        return WebDepsInstaller.install(nodeModulesDir, dependencies);
    }

    protected static Path getNodeModulesDir(Path workDir, BundleOptions bundleOptions) {
        return bundleOptions.nodeModulesDir() == null
                ? workDir.resolve(BundleOptions.NODE_MODULES)
                : bundleOptions.nodeModulesDir();
    }

    public static void clearDependencies(Path nodeModulesDir) throws IOException {
        deleteRecursive(nodeModulesDir);
    }

    protected static DevResult esBuildDev(Path workDir, EsBuildConfig esBuildConfig)
            throws IOException {
        final ScriptRunner scriptRunner = new ScriptRunner(workDir, esBuildConfig);
        return new DevResult(scriptRunner.dev());
    }

    protected static void esBuild(Path workDir, EsBuildConfig esBuildConfig) throws IOException {
        final ScriptRunner scriptRunner = new ScriptRunner(workDir, esBuildConfig);
        scriptRunner.build();
    }

}
