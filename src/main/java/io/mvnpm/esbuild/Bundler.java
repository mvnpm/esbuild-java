package io.mvnpm.esbuild;

import static io.mvnpm.esbuild.util.PathUtils.deleteRecursive;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import io.mvnpm.esbuild.install.WebDepsInstaller;
import io.mvnpm.esbuild.model.BundleOptions;
import io.mvnpm.esbuild.model.BundleResult;
import io.mvnpm.esbuild.model.EsBuildConfig;
import io.mvnpm.esbuild.model.ExecuteResult;
import io.mvnpm.esbuild.resolve.Resolver;

public class Bundler {
    private static final Logger logger = Logger.getLogger(Bundler.class.getName());

    public static final String ESBUILD_EMBEDDED_VERSION = resolveEmbeddedVersion();

    private static String resolveEmbeddedVersion() {
        Properties properties = new Properties();
        try {
            final InputStream resource = Bundler.class.getResourceAsStream("/version.properties");
            if (resource != null) {
                properties.load(resource);
            }
        } catch (IOException e) {
            // ignore we use the default
        }
        String version = properties.getProperty("esbuild.version");
        return requireNonNull(version, "Make sure the version.properties contains 'esbuild.version'.");
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
        final String out = bundleOptions.esBuildConfig().outdir() != null ? bundleOptions.esBuildConfig().outdir() : "dist";
        final Path dist = workDir.resolve(out);
        final EsBuildConfig esBuildConfig = prepareForBundling(bundleOptions, workDir, dist, false);

        final ExecuteResult executeResult = esBuild(workDir, esBuildConfig);

        if (!Files.isDirectory(dist)) {
            throw new BundleException("Unexpected Error during bundling", executeResult.output());
        }

        return new BundleResult(dist, workDir, executeResult);
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

    public static Watch watch(BundleOptions bundleOptions, BuildEventListener eventListener, boolean install)
            throws IOException {
        final Path workDir = getWorkDir(bundleOptions);
        if (install) {
            install(workDir, bundleOptions);
        }
        final String out = bundleOptions.esBuildConfig().outdir() != null ? bundleOptions.esBuildConfig().outdir() : "dist";
        final Path dist = workDir.resolve(out);
        final EsBuildConfig esBuildConfig = prepareForBundling(bundleOptions, workDir, dist, true);

        final Process process = esBuild(workDir, esBuildConfig, eventListener);
        return new Watch(process, workDir, dist);
    }

    private static Path getWorkDir(BundleOptions bundleOptions) throws IOException {
        return bundleOptions.workDir() != null ? bundleOptions.workDir()
                : Files.createTempDirectory("bundle");
    }

    public static boolean install(Path workDir, BundleOptions bundleOptions) throws IOException {
        final Path nodeModulesDir = getNodeModulesDir(workDir, bundleOptions);
        return WebDepsInstaller.install(nodeModulesDir, bundleOptions.dependencies());
    }

    protected static Path getNodeModulesDir(Path workDir, BundleOptions bundleOptions) {
        return bundleOptions.nodeModulesDir() == null
                ? workDir.resolve(BundleOptions.NODE_MODULES)
                : bundleOptions.nodeModulesDir();
    }

    public static void clearDependencies(Path nodeModulesDir) throws IOException {
        deleteRecursive(nodeModulesDir);
    }

    protected static Process esBuild(Path workDir, EsBuildConfig esBuildConfig, BuildEventListener listener)
            throws IOException {
        final Execute execute = getExecute(workDir, esBuildConfig);
        return execute.execute(listener);
    }

    protected static ExecuteResult esBuild(Path workDir, EsBuildConfig esBuildConfig) throws IOException {
        final Execute execute = getExecute(workDir, esBuildConfig);
        return execute.executeAndWait();
    }

    private static Execute getExecute(Path workDir, EsBuildConfig esBuildConfig) throws IOException {
        final String version = esBuildConfig.esBuildVersion() != null ? esBuildConfig.esBuildVersion()
                : Bundler.ESBUILD_EMBEDDED_VERSION;
        final Path esBuildExec = Resolver.create().resolve(version);
        return new Execute(workDir, esBuildExec.toFile(), esBuildConfig);
    }
}
