package ch.nerdin.esbuild.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Comparator;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Copy {

    public static void copyFolder(Path src, Path dest) {
        try (Stream<Path> stream = Files.walk(src)) {
            stream.forEach(source -> copy(source, dest.resolve(src.relativize(source))));
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        try (final Stream<Path> paths = Files.walk(source)) {
            paths.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    public static void smartMove(Path source, Path dest) throws IOException {
        FileSystemProvider providerSrc = source.getFileSystem().provider();
        FileSystemProvider providerDest = source.getFileSystem().provider();

        if (providerSrc == providerDest) {
            Files.move(source, dest);
        } else {
            copyFolder(source, dest);
            deleteRecursive(source);
        }
    }
}
