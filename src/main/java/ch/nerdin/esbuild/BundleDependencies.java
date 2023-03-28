package ch.nerdin.esbuild;

import ch.nerdin.esbuild.resolve.ExecutableResolver;
import ch.nerdin.esbuild.util.EntryPoint;
import ch.nerdin.esbuild.util.ImportToPackage;
import ch.nerdin.esbuild.util.UnZip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class BundleDependencies {
    private static final String IMPORT_FILE_NAME = "META-INF/importmap.json";
    private static final String MVNPM_PACKAGE_PREFIX = "resources/_static";
    private static final String WEBJAR_PACKAGE_PREFIX = "META-INF/resources/webjars";
    public static final String ESBUILD_VERSION = "0.17.10";

    public enum BundleType {
        WEBJAR,
        MVNPM,
    }

    /**
     * Use esbuild to bundle either webjar or mvnpm dependencies into a bundle.
     * @param dependencies the locations of the webjar or mvnpm bundles
     * @param type to indicate the type of bundles, so either web_jar or mvnpm
     * @param entry the entry javascript file
     * @return the folder that has the result of the transformation
     * @throws IOException when something could not be written
     */
    public static Path bundle(List<Path> dependencies, BundleType type, Path entry) throws IOException {
        return bundle(dependencies, type, List.of(entry));
    }

    public static Path bundle(List<Path> dependencies, BundleType type, List<Path> entries) throws IOException {
        return bundle(dependencies, type, entries, useDefaultConfig());
    }

    public static Path bundle(List<Path> dependencies, BundleType type, Path entry, Config config) throws IOException {
        return bundle(dependencies, type, List.of(entry), config);
    }

    public static Path bundle(List<Path> dependencies, BundleType type, List<Path> entries, Config config) throws IOException {
        final Path location = createWorkingTempFolder(dependencies, type, entries);
        final Path dist = location.resolve("dist");

        final Path entry = createOneEntryPointScript(entries, location);
        config.setOutDir(dist.toString());
        config.setEntryPoint(entry.toFile().toString());

        esBuild(config);

        return dist;
    }

    private static Path createOneEntryPointScript(List<Path> entries, Path location) throws IOException {
        final String entryString = EntryPoint.convert(entries.stream().map(Path::toFile).collect(Collectors.toList()));
        final Path entry = location.resolve("index.js");
        Files.writeString(entry, entryString);
        return entry;
    }

    private static Config useDefaultConfig() {
        return new ConfigBuilder().bundle().minify().sourceMap().splitting().format(Config.Format.ESM).build();
    }

    private static Path createWorkingTempFolder(List<Path> dependencies, BundleType type, List<Path> entries) throws IOException {
        final Path location = extract(dependencies, type);
        for (Path entry : entries) {
            copy(entry, location);
        }
        return location;
    }

    private static void copy(Path entry, Path location) throws IOException {
        final Path target = location.resolve(entry.getFileName());
        Files.copy(entry, target, REPLACE_EXISTING);
    }

    protected static Path extract(List<Path> dependencies, BundleType type) throws IOException {
        final Path bundleDirectory = Files.createTempDirectory("bundle");
        final Path nodeModules = bundleDirectory.resolve("node_modules");
        nodeModules.toFile().mkdir();

        for (Path path : dependencies) {
            UnZip.unzip(path, bundleDirectory);
            final NameVersion nameVersion = parseName(path.getFileName().toString());
            switch (type) {
                case MVNPM -> createPackage(bundleDirectory, nameVersion);
                case WEBJAR -> Files.move(bundleDirectory.resolve(WEBJAR_PACKAGE_PREFIX).resolve(nameVersion.name)
                        .resolve(nameVersion.version), nodeModules.resolve(nameVersion.name));
            }
        }

        return bundleDirectory;
    }

    protected static void esBuild(Config config) throws IOException {
        final Path esBuildExec = new ExecutableResolver().resolve(BundleDependencies.ESBUILD_VERSION);
        new Execute(esBuildExec.toFile(), config).execute();
    }

    private static NameVersion parseName(String fileName) {
        final int separatorIndex = fileName.indexOf("-");
        String name = fileName.substring(0, separatorIndex);
        String version = fileName.substring(separatorIndex + 1, fileName.lastIndexOf('.'));

        return new NameVersion(name, version);
    }

    private static void createPackage(Path location, NameVersion nameVersion) throws IOException {
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


