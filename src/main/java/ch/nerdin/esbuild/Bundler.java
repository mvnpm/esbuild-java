package ch.nerdin.esbuild;

import ch.nerdin.esbuild.modal.BundleOptions;
import ch.nerdin.esbuild.modal.EsBuildConfig;
import ch.nerdin.esbuild.resolve.ExecutableResolver;
import ch.nerdin.esbuild.util.ImportToPackage;
import ch.nerdin.esbuild.util.UnZip;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

public class Bundler {


    private static final String WEBJAR_PACKAGE_PREFIX = "META-INF/resources/webjars";
    private static String VERSION;

    public enum BundleType {
        WEBJARS,
        MVNPM,
    }

    public static String getDefaultVersion() {
        if (VERSION == null) {
            final InputStream resource = Bundler.class.getResourceAsStream("/version.properties");
            Properties properties = new Properties();
            try {
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
     * @param bundleOptions
     * @return the folder that has the result of the transformation
     * @throws IOException when something could not be written
     */
    public static Path bundle(BundleOptions bundleOptions) throws IOException {
        final Path location = createWorkingTempFolder(bundleOptions.getDependencies(), bundleOptions.getType());
        final Path dist = location.resolve("dist");

        final EsBuildConfig esBuildConfig = createBundle(bundleOptions, location, dist);

        esBuild(esBuildConfig, null);

        return dist;
    }

    private static EsBuildConfig createBundle(BundleOptions bundleOptions, Path location, Path dist) {
        final EsBuildConfig esBuildConfig = bundleOptions.getEsBuildConfig();
        esBuildConfig.setOutDir(dist.toString());
        final List<String> paths = bundleOptions.getEntries().stream().map(entry -> entry.getEntry(location).toString()).toList();
        esBuildConfig.setEntryPoint(paths.toArray(new String[0]));
        return esBuildConfig;
    }

    public static Watch watch(BundleOptions bundleOptions, BuildEventListener eventListener) throws IOException {
        final Path location = createWorkingTempFolder(bundleOptions.getDependencies(), bundleOptions.getType());
        final Path dist = location.resolve("dist");
        final EsBuildConfig esBuildConfig = createBundle(bundleOptions, location, dist);

        bundleOptions.getEsBuildConfig().setWatch(true);
        final Process process = esBuild(esBuildConfig, eventListener);

        return new Watch(process, location, bundleOptions.getType());
    }

    private static Path createWorkingTempFolder(List<Path> dependencies, BundleType type) throws IOException {
        final Path bundleDirectory = Files.createTempDirectory("bundle");
        return extract(bundleDirectory, dependencies, type);
    }

    protected static Path extract(Path bundleDirectory, List<Path> dependencies, BundleType type) throws IOException {
        final Path nodeModules = bundleDirectory.resolve("node_modules");
        if (!Files.exists(nodeModules)) {
            nodeModules.toFile().mkdir();
        }

        for (Path path : dependencies) {
            UnZip.unzip(path, bundleDirectory);
            final NameVersion nameVersion = parseName(path.getFileName().toString());
            switch (type) {
                case MVNPM -> ImportToPackage.createPackage(bundleDirectory, nameVersion.name, nameVersion.version);
                case WEBJARS -> Files.move(bundleDirectory.resolve(WEBJAR_PACKAGE_PREFIX).resolve(nameVersion.name)
                        .resolve(nameVersion.version), nodeModules.resolve(nameVersion.name));
            }
        }

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

    private static NameVersion parseName(String fileName) {
        final int separatorIndex = fileName.lastIndexOf("-");
        String name = fileName.substring(0, separatorIndex);
        String version = fileName.substring(separatorIndex + 1, fileName.lastIndexOf('.'));

        return new NameVersion(name, version);
    }

    static class NameVersion {
        public String name;
        public String version;

        public NameVersion(String name, String version) {
            this.name = name;
            this.version = version;
        }
    }
}


