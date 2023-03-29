package ch.nerdin.esbuild.util;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

public class ImportToPackage {
    private static final String MVNPM_PACKAGE_PREFIX = "resources/_static";
    private static final String IMPORT_FILE_NAME = "META-INF/importmap.json";

    protected static String convert(String name, String version, String main) {
        return QuteTemplateRenderer.render("package-template.json", Map.of("name", name, "version", version, "main", main));
    }

    protected static String[] extractInfo(Path importMapFile) {
        try {
            final JSONTokener jsonTokener = new JSONTokener(new FileInputStream(importMapFile.toFile()));
            final JSONObject object = new JSONObject(jsonTokener);

            final JSONObject imports = object.getJSONObject("imports");
            final Iterator<String> keys = imports.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                final String value = imports.getString(key);
                if (value.contains(".js")) {
                    return new String[]{key, value};
                }
            }
            throw new RuntimeException("could not find script in import map");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createPackage(Path location, String moduleName, String version) throws IOException {
        final Path importMapFile = location.resolve(IMPORT_FILE_NAME);
        final String[] result = extractInfo(importMapFile);
        final String name = result[0];
        final String packageContents = convert(name, version,  result[1].substring( result[1].indexOf(name) + name.length() + 1));


        final Path modulePath = importMapFile.getParent().resolve(MVNPM_PACKAGE_PREFIX).resolve(moduleName);
        final Path packageFile = modulePath.resolve("package.json");

        Files.writeString(packageFile, packageContents);

        final Path nodeModules = location.resolve("node_modules").resolve(name);
        final File parent = nodeModules.getParent().toFile();
        // in case there is a / in the name
        if (!parent.exists()) parent.mkdirs();
        Files.move(packageFile.getParent(), nodeModules);
    }
}
