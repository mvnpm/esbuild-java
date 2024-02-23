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

    private static final String DIST = "dist";
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
        final Path dist = workDir.resolve(DIST);
        final EsBuildConfig esBuildConfig = createBundle(bundleOptions, workDir, dist);

        final ExecuteResult executeResult = esBuild(workDir, esBuildConfig);

        if (!Files.isDirectory(dist)) {
            throw new BundleException("Unexpected Error during bundling", executeResult.output());
        }

        return new BundleResult(dist, executeResult);
    }

    private static EsBuildConfig createBundle(BundleOptions bundleOptions, Path workDir, Path dist) throws IOException {
        final EsBuildConfig esBuildConfig = bundleOptions.getEsBuildConfig();
        // Clean the dist directory from a previous bundling
        deleteRecursive(dist);
        Files.createDirectories(dist);
        esBuildConfig.setOutdir(dist.toString());
        if (bundleOptions.getEntries() == null) {
            throw new IllegalArgumentException("At least one entry point is required");
        }
        final List<String> paths = bundleOptions.getEntries().stream().map(entry -> entry.process(workDir).toString()).toList();
        esBuildConfig.setEntryPoint(paths.toArray(String[]::new));
        return esBuildConfig;
    }

    public static Watch watch(BundleOptions bundleOptions, BuildEventListener eventListener) throws IOException {
        final Path workDir = getWorkDir(bundleOptions);
        install(workDir, bundleOptions);
        final Path dist = workDir.resolve(DIST);
        final EsBuildConfig esBuildConfig = createBundle(bundleOptions, workDir, dist);

        bundleOptions.getEsBuildConfig().setWatch(true);
        final Process process = esBuild(workDir, esBuildConfig, eventListener);
        return new Watch(process, workDir);
    }

    private static Path getWorkDir(BundleOptions bundleOptions) throws IOException {
        return bundleOptions.getWorkDir() != null ? bundleOptions.getWorkDir()
                : Files.createTempDirectory("bundle");
    }

    public static void install(Path workDir, BundleOptions bundleOptions) throws IOException {
        final Path nodeModulesDir = getNodeModulesDir(workDir, bundleOptions);
        WebDepsInstaller.install(nodeModulesDir, bundleOptions.getDependencies());
    }

    private static Path getNodeModulesDir(Path workDir, BundleOptions bundleOptions) {
        return bundleOptions.getNodeModulesDir() == null
                ? workDir.resolve(BundleOptions.NODE_MODULES)
                : bundleOptions.getNodeModulesDir();
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
        final String version = esBuildConfig.getEsBuildVersion() != null ? esBuildConfig.getEsBuildVersion()
                : Bundler.ESBUILD_EMBEDDED_VERSION;
        final Path esBuildExec = Resolver.create().resolve(version);
        return new Execute(workDir, esBuildExec.toFile(), esBuildConfig);
    }
}
