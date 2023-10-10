package io.mvnpm.esbuild.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class PackageJsonCreator {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ObjectNode browser = mapper.createObjectNode();

    private PackageJsonCreator() {
    }

    public static void createPackageJson(Path root, String name, String version, String main) {
        try {
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            ObjectNode content = createNode(name, version, main);
            Path packageJson = root.resolve("package.json");
            writer.writeValue(Files.newOutputStream(packageJson), content);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static ObjectNode createNode(String name, String version, String main) {
        ObjectNode node = mapper.createObjectNode();

        node.put("name", name);
        node.put("version", version);
        node.put("main", main);
        node.set("browser", browser);

        return node;
    }

    static {
        browser.put("fs", false);
        browser.put("path", false);
        browser.put("os", false);
    }
}
