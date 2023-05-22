package ch.nerdin.esbuild;

import ch.nerdin.esbuild.modal.BundleOptions;
import ch.nerdin.esbuild.modal.EsBuildConfig;
import ch.nerdin.esbuild.resolve.ExecutableResolver;
import ch.nerdin.esbuild.util.Copy;
import ch.nerdin.esbuild.util.PackageJson;
import ch.nerdin.esbuild.util.UnZip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class Bundler {
    private static final Logger logger = LoggerFactory.getLogger(Bundler.class);

    private static final String WEBJAR_PACKAGE_PREFIX = "META-INF/resources/webjars";
    private static final String MVNPM_PACKAGE_PREFIX = "META-INF/resources/_static";
    private static String VERSION;

    public enum BundleType {
        WEBJARS,
        MVNPM,
    }

    public static String getDefaultVersion() {
        if (VERSION == null) {
            Properties properties = new Properties();
            try {
                final InputStream resource = Bundler.class.getResourceAsStream("/version.properties");
                properties.load(resource);
            } catch (IOException e) {
                // ignore we use the default
            }
            VERSION = properties.getProperty("esbuild.version", "0.17.17");
        }

        return VERSION;
    }

    /**
     * Use esbuild to bundle either webjar or mvnpm dependencies into a bundle.
     *
     * @param bundleOptions options to do the bundling with
     * @return the folder that has the result of the transformation
     * @throws IOException when something could not be written
     */
    public static Path bundle(BundleOptions bundleOptions) throws IOException {
        final Path location = extractDependencies(bundleOptions);
        final Path dist = location.resolve("dist");

        final EsBuildConfig esBuildConfig = createBundle(bundleOptions, location, dist);

        esBuild(esBuildConfig, null);

        return dist;
    }

    private static EsBuildConfig createBundle(BundleOptions bundleOptions, Path location, Path dist) throws IOException {
        final EsBuildConfig esBuildConfig = bundleOptions.getEsBuildConfig();
        Files.createDirectories(dist);
        esBuildConfig.setOutdir(dist.toString());

        final Path path = bundleOptions.getWorkFolder() != null ? bundleOptions.getWorkFolder() : location;
        final List<String> paths = bundleOptions.getEntries().stream().map(entry -> entry.process(path).toString()).toList();
        esBuildConfig.setEntryPoint(paths.toArray(new String[0]));
        return esBuildConfig;
    }

    public static Watch watch(BundleOptions bundleOptions, BuildEventListener eventListener) throws IOException {
        final Path location = extractDependencies(bundleOptions);
        final Path dist = location.resolve("dist");
        final EsBuildConfig esBuildConfig = createBundle(bundleOptions, location, dist);

        bundleOptions.getEsBuildConfig().setWatch(true);
        final Process process = esBuild(esBuildConfig, eventListener);

        return new Watch(process, location, bundleOptions.getType());
    }

    private static Path extractDependencies(BundleOptions bundleOptions) throws IOException {
        final Path bundleDirectory = bundleOptions.getWorkFolder() != null ? bundleOptions.getWorkFolder() : Files.createTempDirectory("bundle");
        return extract(bundleDirectory, bundleOptions.getDependencies(), bundleOptions.getType());
    }

    protected static Path extract(Path bundleDirectory, List<Path> dependencies, BundleType type) throws IOException {
        final Path nodeModules = bundleDirectory.resolve("node_modules");
        if (!Files.exists(nodeModules)) {
            Files.createDirectories(nodeModules);
        }
        final Path tmp = Files.createTempDirectory(nodeModules, "extract");
        for (Path path : dependencies) {
            final String fileName = path.getFileName().toString();
            final Path extractDir = tmp.resolve(fileName.substring(0, fileName.lastIndexOf(".")));
            UnZip.unzip(path, extractDir);
            final Optional<Path> packageJson = switch (type) {
                case MVNPM -> PackageJson.findPackageJson(extractDir.resolve(MVNPM_PACKAGE_PREFIX));
                case WEBJARS -> PackageJson.findPackageJson(extractDir.resolve(WEBJAR_PACKAGE_PREFIX));
            };
            if(packageJson.isPresent()) {
                final String packageName = PackageJson.readPackageName(packageJson.get());
                final Path source = packageJson.get().getParent();
                final Path target = nodeModules.resolve(packageName);
                if (!Files.isDirectory(target)) {
                    Files.createDirectories(target.getParent());
                    Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    logger.info("skipping package as it already exists '{}'", target);
                }
            } else {
                logger.info("package.json not found in package '{}'", fileName);
            }
        }
        Copy.deleteRecursive(tmp);
        return bundleDirectory;
    }

    protected static Process esBuild(EsBuildConfig esBuildConfig, BuildEventListener listener) throws IOException {
        final Path esBuildExec = new ExecutableResolver().resolve(Bundler.getDefaultVersion());
        final Execute execute = new Execute(esBuildExec.toFile(), esBuildConfig);
        if (listener != null) {
            return execute.execute(listener);
        } else {
            execute.executeAndWait();
        }
        return null;
    }


}


