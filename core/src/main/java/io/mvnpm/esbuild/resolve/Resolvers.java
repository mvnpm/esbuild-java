package io.mvnpm.esbuild.resolve;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;

final class Resolvers {
    private static final Logger logger = Logger.getLogger(Resolvers.class.getName());

    private Resolvers() {
    }

    public static final String MVNPM_TGZ_PATH_TEMPLATE = "/esbuild-%1$s-%2$s.tgz";
    public static final String ESBUILD_TGZ_PATH_TEMPLATE = "/%1$s-%2$s.tgz";

    private static final String PATH = "package/bin/esbuild";

    private static final String LEGACY_PATH = "package/esbuild";

    static final String CLASSIFIER = determineClassifier();

    static Path resolveExecutablePath(Path bundleDir) {
        String path;
        if (isLegacyBundle(bundleDir)) {
            path = LEGACY_PATH;
        } else {
            path = PATH;
        }
        return bundleDir.resolve(isWindows() ? path + ".exe" : path);
    }

    static Path resolveSassExecutablePath(Path bundleDir) {
        String path = "dart-sass/sass";
        return bundleDir.resolve(isWindows() ? path + ".bat" : path);
    }

    static Path getExecutablePath(Path bundleDir) throws IOException {
        final Path path = resolveExecutablePath(bundleDir);
        if (!Files.isExecutable(path)) {
            return null;
        }
        if (bundleDir.toString().contains("-mvnpm-")) {
            // check for scss
            if (!Files.isExecutable(resolveSassExecutablePath(bundleDir))) {
                return null;
            }
        }
        return path;
    }

    static Path requireExecutablePath(Path bundleDir) throws IOException {
        final Path path = getExecutablePath(bundleDir);
        if (path == null) {
            throw new IOException("Invalid esbuild executable in: " + bundleDir);
        }
        return path;
    }

    static String getTgzPath(String version) {
        if (version.contains("mvnpm")) {
            return MVNPM_TGZ_PATH_TEMPLATE.formatted(CLASSIFIER, version);
        } else {
            return ESBUILD_TGZ_PATH_TEMPLATE.formatted(CLASSIFIER, version);
        }
    }

    private static boolean isWindows() {
        final String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("win");
    }

    private static String determineClassifier() {
        final String osName = System.getProperty("os.name").toLowerCase();
        final String osArch = System.getProperty("os.arch").toLowerCase();
        String classifier = null;
        if (osName.contains("mac")) {
            if (osArch.equals("aarch64") || osArch.contains("arm")) {
                classifier = "macos-arm64";
            } else {
                classifier = "macos-x64";
            }
        } else if (osName.contains("win")) {
            if (osArch.equals("aarch64") || osArch.equals("arm64")) {
                classifier = "windows-arm64";
            } else if (osArch.contains("64")) {
                classifier = "windows-x64";
            }
        } else {
            if (osArch.equals("aarch64") || osArch.equals("arm64")) {
                classifier = "linux-arm64";
            } else if (osArch.contains("64")) {
                classifier = "linux-x64";
            }
        }
        if (classifier == null) {
            throw new EsbuildResolutionException("Incompatible os: '%s' and arch: '%s' for esbuild".formatted(osName, osArch));
        }
        return classifier;
    }

    static Path extract(InputStream archive, String destination) throws IOException {
        final File destinationFile = createDestination(destination).toFile();
        return extract(archive, destinationFile);
    }

    static Path extract(InputStream archive, File destination) throws IOException {
        if (destination.isDirectory() && destination.exists()) {
            FileUtils.deleteDirectory(destination);
        }
        destination.mkdirs();

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

    static boolean isLegacyBundle(Path location) {
        return !Files.isDirectory(location.resolve("package/bin"));
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
