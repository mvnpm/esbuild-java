package io.quarkus.esbuild;

import io.quarkus.esbuild.util.ImportToPackage;
import io.quarkus.esbuild.util.UnZip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class BundleDependencies {
    private static final String IMPORT_FILE_NAME = "META-INF/importmap.json";
    private static final String MVNPM_PACKAGE_PREFIX = "resources/_static";
    private static final String WEBJAR_PACKAGE_PREFIX = "META-INF/resources/webjars";
    private static final String ESBUILD_VERSION = "0.17.10";

    enum BundleType {
        WEB_JAR,
        MVNPM,
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

    protected void esBuild(Path script) throws IOException {
        final Path esBuildExec = new Download(ESBUILD_VERSION).execute();
        final Config config = new ConfigBuilder().bundle(true).minify(true).outDir(script.getParent().resolve("dist")
                .toString()).entryPoint(script.toFile().toString()).build();
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


