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

public class Bundler {


    private static final String WEBJAR_PACKAGE_PREFIX = "META-INF/resources/webjars";
    public static final String ESBUILD_VERSION = "0.17.10";

    public enum BundleType {
        WEBJARS,
        MVNPM,
    }

    /**
     * Use esbuild to bundle either webjar or mvnpm dependencies into a bundle.
     * @param bundleOptions
     * @return the folder that has the result of the transformation
     * @throws IOException when something could not be written
     */
    public static Path bundle(BundleOptions bundleOptions) throws IOException {
        final Path location = createWorkingTempFolder(bundleOptions.getDependencies(), bundleOptions.getType(), bundleOptions.getEntries());
        final Path dist = location.resolve("dist");

        final Path entry = createOneEntryPointScript(bundleOptions.getBundleName(), bundleOptions.getEntries(), location);
        final EsBuildConfig esBuildConfig = bundleOptions.getEsBuildConfig();
        esBuildConfig.setOutDir(dist.toString());
        esBuildConfig.setEntryPoint(entry.toFile().toString());

        esBuild(esBuildConfig);

        return dist;
    }

    private static Path createOneEntryPointScript(String bundleName, List<Path> entries, Path location) throws IOException {
        final String entryString = EntryPoint.convert(entries.stream().map(Path::toFile).collect(Collectors.toList()));
        final Path entry = location.resolve("%s.js".formatted(bundleName));
        Files.writeString(entry, entryString);
        return entry;
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
                case MVNPM -> ImportToPackage.createPackage(bundleDirectory, nameVersion.name, nameVersion.version);
                case WEBJARS -> Files.move(bundleDirectory.resolve(WEBJAR_PACKAGE_PREFIX).resolve(nameVersion.name)
                        .resolve(nameVersion.version), nodeModules.resolve(nameVersion.name));
            }
        }

        return bundleDirectory;
    }

    protected static void esBuild(EsBuildConfig esBuildConfig) throws IOException {
        final Path esBuildExec = new ExecutableResolver().resolve(Bundler.ESBUILD_VERSION);
        new Execute(esBuildExec.toFile(), esBuildConfig).execute();
    }

    private static NameVersion parseName(String fileName) {
        final int separatorIndex = fileName.indexOf("-");
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


