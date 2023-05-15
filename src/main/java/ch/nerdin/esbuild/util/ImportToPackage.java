package ch.nerdin.esbuild.util;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static ch.nerdin.esbuild.util.Copy.smartMove;

public class ImportToPackage {
    private static final String MVNPM_PACKAGE_PREFIX = "resources";
    private static final String IMPORT_FILE_NAME = "META-INF/importmap.json";

    protected static String convert(String name, String version, String main) {
        return QuteTemplateRenderer.render("package-template.json", Map.of("name", name, "version", version, "main", main));
    }

    protected static List<PackageInfo> extractPackages(Path importMapFile) {
        try {
            final JSONTokener jsonTokener = new JSONTokener(new FileInputStream(importMapFile.toFile()));
            final JSONObject object = new JSONObject(jsonTokener);

            final JSONObject imports = object.getJSONObject("imports");
            final Iterator<String> keys = imports.keys();
            final List<PackageInfo> packages = new ArrayList<>();
            while (keys.hasNext()) {
                String key = keys.next();
                final String value = imports.getString(key);
                if (value.endsWith(".js") || value.endsWith(".mjs") && imports.has(key + "/")) {
                    final String dir;
                    if ((value.endsWith("index.js") || value.endsWith(".mjs"))
                            && !hasScripts(importMapFile.getParent().resolve(MVNPM_PACKAGE_PREFIX + imports.getString(key + "/")))) {
                        // This is an index and the importmap dir does not contain any script
                        // use the index parent directory as package root
                        dir = value.substring(0, value.lastIndexOf("/") + 1);
                    } else {
                        dir = imports.getString(key + "/");
                    }
                    packages.add(new PackageInfo(key, value.substring(dir.length()), dir));
                }
            }
            return packages;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean hasScripts(Path dir) throws IOException {
        try (Stream<Path> list = Files.list(dir)) {
            return list.map(Path::getFileName)
                    .map(Path::toString)
                    .anyMatch(n -> n.endsWith(".js") || n.endsWith(".mjs") || n.endsWith(".css"));
        }
    }


    public static void createPackage(Path nodeModules, Path location, String version) throws IOException {
        final Path importMapFile = location.resolve(IMPORT_FILE_NAME);
        if (!Files.isRegularFile(importMapFile)) {
            // This is not a valid MVNPM dependency
            return;
        }
        final List<PackageInfo> packages = extractPackages(importMapFile);
        for (PackageInfo info : packages) {
            final String packageContents = convert(info.getName(), version, info.getMain());
            final Path packageDir = importMapFile.getParent().resolve(MVNPM_PACKAGE_PREFIX + info.getDirectory());
            if (!Files.isDirectory(packageDir)) {
                throw new IllegalStateException("Invalid MVNPM dependency structure: " + packageDir);
            }
            final Path packageFile = packageDir.resolve("package.json");
            Files.writeString(packageFile, packageContents);

            // in case there is a / in the name
            final Path targetDir = nodeModules.resolve(info.getName());
            if (!Files.exists(targetDir.getParent())) Files.createDirectories(targetDir.getParent());
            smartMove(packageDir, targetDir);
        }
    }

    static class PackageInfo {
        private final String name;
        private final String main;

        private final String directory;

        PackageInfo(String name, String main, String directory) {
            this.name = name;
            this.main = main;
            this.directory = directory;
        }

        public String getName() {
            return name;
        }

        public String getMain() {
            return main;
        }

        public String getDirectory() {
            return directory;
        }
    }
}
