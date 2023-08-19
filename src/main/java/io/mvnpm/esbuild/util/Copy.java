package io.mvnpm.esbuild.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Copy {

    public static void copyEntries(Path rootDir, List<String> entries, Path targetDir) {
        for (String entry : entries) {
            try {
                final Path src = rootDir.resolve(entry);
                if (!Files.exists(src)) {
                    throw new IOException("Entry file not found: " + src);
                }
                final Path dest = targetDir.resolve(entry);
                if (!Files.exists(dest)) {
                    Files.createDirectories(dest.getParent());
                    Files.copy(src, dest);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void copy(Path source, Path dest) {
        try {
            Files.copy(source, dest, REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteRecursive(Path source) throws IOException {
        if (!Files.exists(source)){
            return;
        }
        try (final Stream<Path> paths = Files.walk(source)) {
            paths.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}
