package io.mvnpm.esbuild.resolve;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

public abstract class BaseResolver {
    private static final String PATH = "package/bin/esbuild";

    private static final String WINDOWS_EXE_PATH = "package/esbuild.exe";

    public static final String CLASSIFIER = determineClassifier();

    protected Resolver resolver;

    public BaseResolver(Resolver resolver) {
        this.resolver = requireNonNull(resolver, "resolver is required");
    }

    public static String resolveBundledExecutablePath() {
        return isWindows() ? PATH + ".exe" : PATH;
    }

    public static String resolveExecutablePath() {
        return isWindows() ? WINDOWS_EXE_PATH : PATH;
    }

    private static boolean isWindows() {
        final String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("win");
    }

    private static String determineClassifier() {
        final String osName = System.getProperty("os.name").toLowerCase();
        final String osArch = System.getProperty("os.arch").toLowerCase();
        String classifier;

        if (osName.contains("mac")) {
            if (osArch.equals("aarch64") || osArch.contains("arm")) {
                classifier = "darwin-arm64";
            } else {
                classifier = "darwin-x64";
            }
        } else if (osName.contains("win")) {
            classifier = osArch.contains("64") ? "win32-x64" : "win32-ia32";
        } else {
            if (osArch.equals("aarch64") || osArch.equals("arm64")) {
                classifier = "linux-arm64";
            } else if (osArch.contains("arm")) {
                classifier = "linux-arm";
            } else if (osArch.contains("64")) {
                classifier = "linux-x64";
            } else {
                classifier = "linux-ia32";
            }
        }
        return classifier;
    }

    static Path extract(InputStream archive, String destination) throws IOException {
        final File destinationFile = createDestination(destination).toFile();
        return extract(archive, destinationFile);
    }

    static Path extract(InputStream archive, File destination) throws IOException {
        if (!destination.exists()) {
            destination.mkdirs();
        }

        try (GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(archive);
                TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {

            ArchiveEntry entry;
            while ((entry = tarIn.getNextEntry()) != null) {
                if (!tarIn.canReadEntryData(entry) || entry.isDirectory()) {
                    // Entry is a directory or symbolic link, skip it
                    continue;
                }

                // Create a file for this entry in the output directory
                File outputFile = new File(destination, entry.getName());
                // Ensure that the parent directory exists
                File parentDir = outputFile.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }

                if (Files.exists(outputFile.toPath())) {
                    Files.delete(outputFile.toPath());
                }

                // Create the output file with its original permissions
                Files.createFile(outputFile.toPath());

                // Get the POSIX file permissions from the TarArchiveEntry
                int mode = ((TarArchiveEntry) entry).getMode();
                Set<PosixFilePermission> permissions = convertModeToPosixFilePermissions(mode);
                try {
                    Files.setPosixFilePermissions(outputFile.toPath(), permissions);
                } catch (UnsupportedOperationException e) {
                    // ignore we are on a platform that doesn't support this
                }

                try (OutputStream outputStream = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = tarIn.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            }
        }

        return destination.toPath();
    }

    static Path createDestination(String version) throws IOException {
        return Files.createDirectories(getLocation(version));
    }

    static Path getLocation(String version) {
        return Path.of(System.getProperty("java.io.tmpdir")).resolve("esbuild-" + version);
    }

    // Helper method to convert Tar mode to PosixFilePermission
    private static Set<PosixFilePermission> convertModeToPosixFilePermissions(int mode) {
        Set<PosixFilePermission> permissions = new HashSet<>(Arrays.asList(PosixFilePermission.values()));

        for (int i = 0; i < 9; i++) {
            int mask = 1 << (8 - i);
            if ((mode & mask) == 0) {
                // Bit is not set, remove the permission
                permissions.remove(getPermissionForIndex(i));
            }
        }

        return permissions;
    }

    private static PosixFilePermission getPermissionForIndex(int index) {
        switch (index) {
            case 0:
                return PosixFilePermission.OWNER_READ;
            case 1:
                return PosixFilePermission.OWNER_WRITE;
            case 2:
                return PosixFilePermission.OWNER_EXECUTE;
            case 3:
                return PosixFilePermission.GROUP_READ;
            case 4:
                return PosixFilePermission.GROUP_WRITE;
            case 5:
                return PosixFilePermission.GROUP_EXECUTE;
            case 6:
                return PosixFilePermission.OTHERS_READ;
            case 7:
                return PosixFilePermission.OTHERS_WRITE;
            case 8:
                return PosixFilePermission.OTHERS_EXECUTE;
            default:
                throw new IllegalArgumentException("Invalid index: " + index);
        }
    }
}
