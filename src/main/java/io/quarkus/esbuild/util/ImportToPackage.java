package io.quarkus.esbuild.util;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ImportToPackage {

    protected static String convert(String name, String version, String main) {
        Engine engine = Engine.builder().addDefaults().build();
        final URL location = ImportToPackage.class.getResource("/package-template.json");
        final List<String> template;
        try {
            template = Files.readAllLines(new File(location.toURI()).toPath());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        final Template packageJson = engine.parse(String.join("\n", template));
        return packageJson.data("name", name, "version", version, "main", main).render();
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
