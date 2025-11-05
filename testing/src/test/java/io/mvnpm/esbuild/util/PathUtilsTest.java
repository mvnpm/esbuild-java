package io.mvnpm.esbuild.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PathUtilsTest {

    private static final Random rand = new Random();

    /**
     * Tests a straightforward move, with no expected issues
     *
     * @throws IOException when an underlying io error occurs
     */
    @Test
    public void safeMove() throws IOException {

        // create directory with files
        final SafeMoveTestData data = createFiles("safeMove");
        final Path source = data.source;
        final Path target = source.resolveSibling("target");
        Files.createDirectories(target);

        // move files to new location
        PathUtils.safeMove(source, target);

        // ensure files are gone from source place and exist in target
        checkSourceAndTarget(data, target);
    }

    /**
     * Test with a file (or files) open to see what happens
     *
     * @throws IOException when an underlying io error occurs
     */
    @Test
    public void safeMoveWithContention() throws IOException {

        // create directory with files
        final SafeMoveTestData data = createFiles("safeMoveWithContention");
        final Path source = data.source;
        final Path target = source.resolveSibling("target");
        Files.createDirectories(target);

        final Path fileToOpen = data.files.get(0);
        try (
                // open file
                final InputStream hold = Files.newInputStream(fileToOpen)) {
            try {
                Files.move(source, target);
                // this might be overkill...
                Assertions.fail("expectation failed: no exception was thrown while moving under contention");
            } catch (FileSystemException shouldAlwaysThrow) {
                // no-op
            }

            // move files to new location
            PathUtils.safeMove(source, target);
        }

        // ensure files are gone from source place and exist in target
        checkSourceAndTarget(data, target);
    }

    /**
     * Given the list of files and the source and target directories this allows both places
     * to ensure that the files have been moved (and not left in) from the source directory to
     * the target.
     *
     * @param data that was generated when the test files were created
     * @param target root of target directory
     */
    private static void checkSourceAndTarget(SafeMoveTestData data, Path target) throws IOException {
        final Path source = data.source;
        // ensure files are gone from source place and exist in target
        for (final Path sourcePath : data.files) {
            if (Files.exists(sourcePath)) {
                Assertions.fail("No source paths should exist after move: " + sourcePath + " still exists");
            }
            // create the target path by first finding the relative path from source base to actual file and then resolve that on the target path
            final Path targetPath = target.resolve(source.relativize(sourcePath));
            if (!Files.exists(targetPath)) {
                Assertions.fail("Every source path should be moved, the path " + targetPath + " is missing");
            }
            final String sourceHash = data.hashes.get(sourcePath);
            final String targetHash = PathUtils.hash(targetPath);

            // typically you would do something like Assertions.equals(sourceHash, targetHash) but the logs of that isn't particularly helpful
            if (!Objects.equals(sourceHash, targetHash)) {
                Assertions
                        .fail(String.format("The content of '%s' does not match the content of '%s'", sourcePath, targetPath));
            }
        }
    }

    /**
     * Simple data class to allow more information to be provided about the created test
     * files.
     */
    private static class SafeMoveTestData {
        Path source;

        List<Path> files = new ArrayList<>();
        Map<Path, String> hashes = new HashMap<>();
    }

    /**
     * Create files for the test scenario
     *
     * @param testName so that the test directory can be named
     * @return the data that was created during the test
     * @throws IOException if an underlying io exception occurs
     */
    private SafeMoveTestData createFiles(final String testName) throws IOException {
        // it is easier to find files if we use the maven target directory as our test root
        final Path mvnTarget = Paths.get("./target");
        Path root;
        if (Files.exists(mvnTarget) && Files.isDirectory(mvnTarget)) {
            final Path tmpRoot = mvnTarget.resolve("tmp");
            Files.createDirectories(tmpRoot);
            root = Files.createTempDirectory(tmpRoot, String.format("%s-test-", testName));
        } else {
            root = Files.createTempDirectory(String.format("%s-test-", testName));
        }
        Files.createDirectories(root);

        // create source and target directories
        final Path source = root.resolve("source");
        Files.createDirectories(source);

        // in the source directory create a bunch of files
        final SafeMoveTestData data = new SafeMoveTestData();
        data.source = source;

        // this is our random data buffer, each file should be 8K just for checking that the
        // file data doesn't cause issues
        byte[] buffer = new byte[1024 * 8];

        for (int idx = 0; idx < 100; idx++) {
            Path sourceRoot = source;
            int depth = 0;

            // create arbitrary directories with a 50% chance of creating the next level up to a depth of 5
            while (rand.nextBoolean() && depth < 5) {
                sourceRoot = Files.createTempDirectory(sourceRoot, "dir-");
                depth++;
            }

            // create the logs file where we need it
            final Path created = Files.createTempFile(sourceRoot, "temp-file-", ".data");

            // add some random data to the file, this should cause most of the files to be the size of the buffer
            // but some will have more and some could be large-ish.
            do {
                rand.nextBytes(buffer);
                Files.write(created, buffer);
            } while (rand.nextBoolean());

            // keep the list of created files
            data.files.add(created);
            data.hashes.put(created, PathUtils.hash(created));
        }

        return data;
    }

}
