package io.mvnpm.esbuild.util;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.jboss.logging.Logger;

public class PathUtils {

    private static final Logger LOG = Logger.getLogger(PathUtils.class);

    public static void copyEntries(Path rootDir, List<String> entries, Path targetDir) {
        for (String entry : entries) {
            try {
                final Path src = rootDir.resolve(entry);
                if (!Files.exists(src)) {
                    throw new IOException("Entry file not found: " + src);
                }
                final Path dest = targetDir.resolve(entry);
                if (!Files.exists(dest.getParent())) {
                    Files.createDirectories(dest.getParent());
                }
                Files.copy(src, dest, REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void deleteRecursive(Path source) throws IOException {
        if (!Files.exists(source)) {
            return;
        }
        try (final Stream<Path> paths = Files.walk(source)) {
            paths.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

        }
    }

    /**
     * Provides an implementation, based on {@link Files#move}, that is sensitive
     * to failure and will try and mitigate or otherwise work around
     * transient file system failures that would otherwise cause the
     * build to fail. Implementing this should reduce frustration for
     * users.
     *
     * @param source path to move
     * @param target to move the path to
     * @throws IOException when underlying io exceptions are encountered
     */
    public static void safeMove(final Path source, final Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (FileSystemException exception) {
            // on windows, and possibly some other systems, it is possible that
            // other processes (ide, antivirus, etc) could be accessing the
            // file being deleted. this fallback method attempts to copy then
            // delete instead of moving.
            // the method of going and walking the entire file tree is used because
            // some methods (like Files.copy) can silently fail when one file in the
            // batch fails as in the PathUtilsTest.safeMoveWithContention case.
            LOG.warnf("encountered ''%s'' while moving ''%s'' to ''%s'', falling back to secondary method",
                    new Object[] { exception.getClass().getName(), source, target });

            // this is the list of the files that are copied and not moved so we
            // need to attempt to delete them later
            final List<Path> copied = new ArrayList<>(0);

            try (Stream<Path> paths = Files.walk(source, FileVisitOption.FOLLOW_LINKS)) {
                // copy each path to the matching target on the other side, log errors but keep going
                for (final Path p : paths.toList()) {
                    // no idea why this would happen
                    if (!Files.exists(p)) {
                        return;
                    }
                    final Path targetPath = target.resolve(source.relativize(p));
                    if (Files.isDirectory(p)) {
                        Files.createDirectories(targetPath);
                    } else if (Files.isRegularFile(p)) {
                        Files.createDirectories(targetPath.getParent());
                        try {
                            Files.move(p, targetPath, REPLACE_EXISTING);
                        } catch (FileSystemException moveException) {
                            Files.copy(p, targetPath, REPLACE_EXISTING);
                            copied.add(p);
                        }
                    }
                }
            }

            // clean up/delete *copied* files (not moved files) at the end
            for (final Path toDelete : copied) {
                try {
                    Files.deleteIfExists(toDelete);
                } catch (Exception ex) {
                    LOG.warnf("could not delete ''%s'' after copy", toDelete);
                }
            }

            // ensure source directory is gone
            PathUtils.deleteRecursive(source);
        }
    }

    /**
     * Provides a SHA2-512 hash of the target path (if it is a file) and an
     * empty string if it is not. Throws a runtime exception in the event that
     * the SHA-512 algorithm cannot be used.
     *
     * @param target path to a file to get the hash of
     * @return the hash, as a string of base64 characters, of the file. if it is not a file then an empty string is returned.
     * @throws IOException in the event of an underlying IO error
     * @throws RuntimeException if any runtime exception occurs or if the SHA-512 algorithm is not found
     */
    public static String hash(final Path target) throws IOException {
        if (Files.isRegularFile(target)) {
            try (final InputStream fileInputStream = Files.newInputStream(target, StandardOpenOption.READ)) {
                final MessageDigest md = MessageDigest.getInstance("SHA-512");
                final byte[] buffer = new byte[1024 * 16]; // buffer is just a guess
                int read;
                while ((read = fileInputStream.read(buffer)) >= 0) {
                    md.update(buffer, 0, read);
                }
                byte[] digest = md.digest();
                return Base64.getEncoder().encodeToString(digest);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        return "";
    }
}
