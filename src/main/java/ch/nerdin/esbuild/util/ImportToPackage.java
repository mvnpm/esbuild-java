package ch.nerdin.esbuild.util;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Map;

public class ImportToPackage {

    protected static String convert(String name, String version, String main) {
        return QuteTemplateRenderer.render("package-template.json", Map.of("name", name, "version", version, "main", main));
    }

    protected static String extractInfo(Path importMapFile, String name) {
        try {
            final JSONTokener jsonTokener = new JSONTokener(new FileInputStream(importMapFile.toFile()));
            final JSONObject object = new JSONObject(jsonTokener);

            return object.getJSONObject("imports").getString(name);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String createPackage(Path importMapFile, String name, String version) {
        final String main = extractInfo(importMapFile, name);
        return convert(name, version, main.substring(main.indexOf(name) + name.length() + 1));
    }
}
