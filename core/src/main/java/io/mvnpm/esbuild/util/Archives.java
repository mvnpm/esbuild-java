package io.mvnpm.esbuild.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

public class Archives {

    public static void unzip(Path source, Path target) throws IOException {
        try (var is = Files.newInputStream(source);
                var bis = new BufferedInputStream(is);
                ZipInputStream zip = new ZipInputStream(bis)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                extractEntry(target, entry.getName(), entry.isDirectory(), zip);
            }
        }
    }

    public static void unTgz(Path source, Path target) throws IOException {
        try (var is = Files.newInputStream(source);
                var bis = new BufferedInputStream(is);
                var gzis = new GzipCompressorInputStream(bis);
                var tar = new TarArchiveInputStream(gzis)) {
            TarArchiveEntry entry;
            while ((entry = tar.getNextTarEntry()) != null) {
                extractEntry(target, entry.getName(), entry.isDirectory(), tar);
            }
        }

    }

    private static void extractEntry(Path target, String name, boolean isDirectory, InputStream is) throws IOException {
        Path newPath = entrySlipProtect(name, target);

        if (isDirectory) {
            Files.createDirectories(newPath);
        } else {
            if (newPath.getParent() != null) {
                if (Files.notExists(newPath.getParent())) {
                    Files.createDirectories(newPath.getParent());
                }
            }

            Files.copy(is, newPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Path entrySlipProtect(String name, Path targetDir)
            throws IOException {

        // test zip slip vulnerability
        // Path targetDirResolved = targetDir.resolve("../../" + zipEntry.getName());
        Path targetDirResolved = targetDir.resolve(name);

        // make sure normalized file still has targetDir as its prefix
        // else throws exception
        Path normalizePath = targetDirResolved.normalize();
        if (!normalizePath.startsWith(targetDir)) {
            throw new IOException("Bad entry: " + name);
        }

        return normalizePath;
    }

}
