package io.mvnpm.esbuild.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import io.mvnpm.esbuild.model.EsBuildConfig;

public class EntryCreator {
    private static final String PLACEHOLDER = "<% config %>";

    public static File createEntryJs(Path root, EsBuildConfig config) throws IOException {
        Path entryFile = root.resolve("index.js");
        try (InputStream resource = EntryCreator.class.getResourceAsStream("/template.js")) {
            if (resource == null) {
                throw new IOException("template not found in classpath");
            }
            try (Scanner scanner = new Scanner(resource, StandardCharsets.UTF_8)) {
                String templateContent = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                String entryJs = templateContent.replace(PLACEHOLDER, config.toJson());
                Files.writeString(entryFile, entryJs);
            }
        }

        return entryFile.toFile();
    }
}
