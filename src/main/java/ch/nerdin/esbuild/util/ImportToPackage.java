package ch.nerdin.esbuild.util;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImportToPackage {
    private static final String MVNPM_PACKAGE_PREFIX = "resources";
    private static final String IMPORT_FILE_NAME = "META-INF/importmap.json";

    protected static String convert(String name, String version, String main) {
        return QuteTemplateRenderer.render("package-template.json", Map.of("name", name, "version", version, "main", main));
    }

    protected static PackageInfo extractInfo(Path importMapFile) {
        try {
            final JSONTokener jsonTokener = new JSONTokener(new FileInputStream(importMapFile.toFile()));
            final JSONObject object = new JSONObject(jsonTokener);

            final JSONObject imports = object.getJSONObject("imports");
            final Iterator<String> keys = imports.keys();
            String name = null;
            String dir = null;
            String main = null;
            while (keys.hasNext()) {
                String key = keys.next();
                final String value = imports.getString(key);
                if (value.endsWith(".js") || value.endsWith(".mjs")) {
                    name = key;
                    main = value;
                } else if (value.endsWith("/")) {
                    dir = value;
                }
            }
            if(name == null || dir == null || main == null) {
                throw new RuntimeException("could not find script in import map");
            }
            return new PackageInfo(name, main.substring(dir.length(), main.length()), dir);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public static void createPackage(Path nodeModules, Path location, String moduleName, String version) throws IOException {
        final Path importMapFile = location.resolve(IMPORT_FILE_NAME);
        if(!Files.isRegularFile(importMapFile)) {
            // This is not a valid MVNPM dependency
            return;
        }
        final PackageInfo info = extractInfo(importMapFile);
        final String packageContents = convert(info.getName(), version,  info.getMain());
        final Path packageDir = importMapFile.getParent().resolve(MVNPM_PACKAGE_PREFIX + info.getDirectory());
        if(!Files.isDirectory(packageDir)) {
            throw new IllegalStateException("Invalid MVNPM dependency structure: " + packageDir);
        }
        final Path packageFile = packageDir.resolve("package.json");
        Files.writeString(packageFile, packageContents);

        // in case there is a / in the name
        final Path targetDir = nodeModules.resolve(info.getName());
        if (!Files.exists(targetDir.getParent())) Files.createDirectories(targetDir.getParent());
        Files.move(packageFile.getParent(), targetDir);

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
