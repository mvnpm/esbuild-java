package io.mvnpm.esbuild;

import static io.mvnpm.esbuild.util.Copy.deleteRecursive;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.mvnpm.esbuild.model.BundleOptions;
import io.mvnpm.esbuild.model.BundleResult;
import io.mvnpm.esbuild.model.BundleType;
import io.mvnpm.esbuild.model.EsBuildConfig;
import io.mvnpm.esbuild.model.ExecuteResult;
import io.mvnpm.esbuild.resolve.ExecutableResolver;
import io.mvnpm.esbuild.util.JarInspector;
import io.mvnpm.esbuild.util.UnZip;

public class Bundler {
    private static final Logger logger = Logger.getLogger(Bundler.class.getName());

    private static final String NODE_MODULES = "node_modules";
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
     * @return the folder that has the result of the transformation
     * @throws IOException when something could not be written
     */
    public static BundleResult bundle(BundleOptions bundleOptions) throws IOException {
        final Path workDir = installIfNeeded(bundleOptions);
        final Path dist = workDir.resolve(DIST);
        final EsBuildConfig esBuildConfig = createBundle(bundleOptions, workDir, dist);

        final ExecuteResult executeResult = esBuild(esBuildConfig);

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
        final List<String> paths = bundleOptions.getEntries().stream().map(entry -> entry.process(workDir).toString()).toList();
        esBuildConfig.setEntryPoint(paths.toArray(String[]::new));
        return esBuildConfig;
    }

    public static Watch watch(BundleOptions bundleOptions, BuildEventListener eventListener) throws IOException {
        final Path workDir = installIfNeeded(bundleOptions);
        final Path dist = workDir.resolve(DIST);
        final EsBuildConfig esBuildConfig = createBundle(bundleOptions, workDir, dist);

        bundleOptions.getEsBuildConfig().setWatch(true);
        final Process process = esBuild(esBuildConfig, eventListener);
        return new Watch(process, workDir);
    }

    public static Path installIfNeeded(BundleOptions bundleOptions) throws IOException {
        if (bundleOptions.getWorkDir() != null && Files.isDirectory(bundleOptions.getWorkDir().resolve(NODE_MODULES))) {
            return bundleOptions.getWorkDir();
        }
        return install(bundleOptions);
    }

    public static Path install(BundleOptions bundleOptions) throws IOException {
        final Path workingDir = bundleOptions.getWorkDir() != null ? bundleOptions.getWorkDir()
                : Files.createTempDirectory("bundle");
        return extract(workingDir, bundleOptions.getDependencies(), bundleOptions.getType());
    }

    public static Path install(Path workingDir, List<Path> dependencies, BundleType type) throws IOException {
        return extract(workingDir, dependencies, type);
    }

    public static void clearDependencies(Path workingDir) throws IOException {
        deleteRecursive(workingDir.resolve(NODE_MODULES));
    }

    protected static Path extract(Path bundleDirectory, List<Path> dependencies, BundleType type) throws IOException {
        final Path nodeModules = bundleDirectory.resolve(NODE_MODULES);
        if (!Files.exists(nodeModules)) {
            Files.createDirectories(nodeModules);
        }
        final Path tmp = bundleDirectory.resolve("tmp");
        for (Path path : dependencies) {
            final String fileName = path.getFileName().toString();
            final Path extractDir = tmp.resolve(fileName.substring(0, fileName.lastIndexOf(".")));
            // Only extract new dependencies
            if (!Files.isDirectory(extractDir)) {
                UnZip.unzip(path, extractDir);
                final Map<String, Path> packageNameAndRoot = JarInspector.findPackageNameAndRoot(extractDir, type);

                if (!packageNameAndRoot.isEmpty()) {
                    for (Map.Entry<String, Path> nameAndRoot : packageNameAndRoot.entrySet()) {
                        final String packageName = nameAndRoot.getKey();
                        final Path source = nameAndRoot.getValue();
                        final Path target = nodeModules.resolve(packageName);
                        if (!Files.isDirectory(target)) {
                            Files.createDirectories(target.getParent());
                            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                        } else {
                            logger.log(Level.INFO, "skipping package as it already exists ''{0}''", target);
                        }
                    }
                } else {
                    logger.log(Level.INFO, "package.json not found in package: ''{0}''", fileName);
                }
            }
        }
        return bundleDirectory;
    }

    protected static Process esBuild(EsBuildConfig esBuildConfig, BuildEventListener listener) throws IOException {
        final Path esBuildExec = new ExecutableResolver().resolve(Bundler.ESBUILD_EMBEDDED_VERSION);
        final Execute execute = new Execute(esBuildExec.toFile(), esBuildConfig);
        return execute.execute(listener);
    }

    protected static ExecuteResult esBuild(EsBuildConfig esBuildConfig) throws IOException {
        final Path esBuildExec = new ExecutableResolver().resolve(Bundler.ESBUILD_EMBEDDED_VERSION);
        final Execute execute = new Execute(esBuildExec.toFile(), esBuildConfig);
        return execute.executeAndWait();
    }

}
