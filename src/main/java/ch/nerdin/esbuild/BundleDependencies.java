package ch.nerdin.esbuild;

import ch.nerdin.esbuild.resolve.ExecutableResolver;
import ch.nerdin.esbuild.util.ImportToPackage;
import ch.nerdin.esbuild.util.UnZip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class BundleDependencies {
    private static final String IMPORT_FILE_NAME = "META-INF/importmap.json";
    private static final String MVNPM_PACKAGE_PREFIX = "resources/_static";
    private static final String WEBJAR_PACKAGE_PREFIX = "META-INF/resources/webjars";
    public static final String ESBUILD_VERSION = "0.17.10";

    enum BundleType {
        WEB_JAR,
        MVNPM,
    }

    public Path bundle(List<Path> dependencies, BundleType type, Path entry) throws IOException {
        final Path path = getPath(dependencies, type, entry);
        final Path dist = path.resolve("dist");
        final Config config = new ConfigBuilder().bundle().minify().sourceMap().splitting().format(Config.Format.ESM)
                .outDir(dist.toString()).entryPoint(path.resolve(entry.getFileName()).toFile().toString()).build();

        esBuild(config);

        return dist;
    }

    public Path bundle(List<Path> dependencies, BundleType type, Path entry, Config config) throws IOException {
        final Path path = extract(dependencies, type);
        config.setEntryPoint(path.resolve(entry.getFileName()).toFile().toString());

        esBuild(config);

        return path;
    }

    private Path getPath(List<Path> dependencies, BundleType type, Path entry) throws IOException {
        final Path path = extract(dependencies, type);
        final Path target = path.resolve(entry.getFileName());
        Files.copy(entry, target, REPLACE_EXISTING);
        return path;
    }

    protected Path extract(List<Path> dependencies, BundleType type) throws IOException {
        final Path bundleDirectory = Files.createTempDirectory("bundle");
        final Path nodeModules = bundleDirectory.resolve("node_modules");
        nodeModules.toFile().mkdir();

        for (Path path : dependencies) {
            UnZip.unzip(path, bundleDirectory);
            final NameVersion nameVersion = parseName(path.getFileName().toString());
            switch (type) {
                case MVNPM -> createPackage(bundleDirectory, nameVersion);
                case WEB_JAR -> Files.move(bundleDirectory.resolve(WEBJAR_PACKAGE_PREFIX).resolve(nameVersion.name)
                        .resolve(nameVersion.version), nodeModules.resolve(nameVersion.name));
            }
        }

        return bundleDirectory;
    }

    protected void esBuild(Config config) throws IOException {
        final Path esBuildExec = new ExecutableResolver().resolve(BundleDependencies.ESBUILD_VERSION);
        new Execute(esBuildExec.toFile(), config).execute();
    }

    private NameVersion parseName(String fileName) {
        final int separatorIndex = fileName.indexOf("-");
        String name = fileName.substring(0, separatorIndex);
        String version = fileName.substring(separatorIndex + 1, fileName.lastIndexOf('.'));

        return new NameVersion(name, version);
    }

    private void createPackage(Path location, NameVersion nameVersion) throws IOException {
        String name = nameVersion.name;

        final Path importPackage = location.resolve(IMPORT_FILE_NAME);

        final String packageContents = ImportToPackage.createPackage(importPackage, name, nameVersion.version);
        final Path packageFile = importPackage.getParent().resolve(MVNPM_PACKAGE_PREFIX).resolve(name).resolve("package.json");
        Files.writeString(packageFile, packageContents);

        Files.move(packageFile.getParent(), location.resolve("node_modules").resolve(name));
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


