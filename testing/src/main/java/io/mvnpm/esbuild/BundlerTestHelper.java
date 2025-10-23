package io.mvnpm.esbuild;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.apache.commons.io.FileUtils;

import io.mvnpm.esbuild.model.BundleOptions;
import io.mvnpm.esbuild.model.BundleOptionsBuilder;
import io.mvnpm.esbuild.model.BundleResult;
import io.mvnpm.esbuild.model.WebDependency.WebDependencyType;

public class BundlerTestHelper {

    public static final String ESBUILD_TEST_DIR = "testing/esbuild-test-dir";

    public static void executeTest(List<String> jarNames, WebDependencyType type, String scriptName, boolean check)
            throws URISyntaxException, IOException {
        final BundleOptions bundleOptions = getBundleOptions(jarNames, type, scriptName).build();
        final BundleResult result = Bundler.bundle(bundleOptions, true);
        System.out.println(result.output());
        if (check) {
            System.out.println(result.dist());
            assertTrue(result.dist().toFile().exists());
            assertTrue(Files.list(result.dist()).count() > 0);
        }

    }

    public static Path copyTestScriptsDir(String name) throws IOException {
        final Path path = BundlerTestHelper.locateTestScripts();
        final Path root = path.resolve(name);
        if (!Files.isDirectory(root)) {
            throw new FileNotFoundException("Directory " + root + " not found");
        }
        final Path temp = Files.createTempDirectory("test-" + name);
        FileUtils.copyDirectory(root.toFile(), temp.toFile());
        return temp;
    }

    public static Path locateTestScripts() throws IOException {
        return locateTestScripts(Paths.get("."));
    }

    private static Path locateTestScripts(Path parent) throws IOException {
        if (!Files.exists(parent)) {
            throw new FileNotFoundException(ESBUILD_TEST_DIR + " not found.");
        }
        if (Files.isDirectory(parent.resolve(ESBUILD_TEST_DIR))) {
            return parent.resolve(ESBUILD_TEST_DIR).normalize();
        }
        return locateTestScripts(parent.resolve(".."));
    }

    public static BundleOptionsBuilder getBundleOptions(List<String> jarNames, WebDependencyType type, String scriptName)
            throws URISyntaxException, IOException {
        final List<Path> jars = getJars(jarNames);
        final Path tempDir = Files.createTempDirectory("esbuild-test");
        final BundleOptionsBuilder bundleOptionsBuilder = BundleOptions.builder().withDependencies(jars, type);
        if (scriptName != null) {
            final Path testDir = locateTestScripts();
            Files.copy(testDir.resolve(scriptName), tempDir.resolve(scriptName), StandardCopyOption.REPLACE_EXISTING);
            bundleOptionsBuilder
                    .addEntryPoint(tempDir, scriptName);
        }
        return bundleOptionsBuilder;
    }

    public static List<Path> getJars(List<String> jarNames) {
        final List<Path> jars = jarNames.stream().map(jarName -> {
            try (InputStream inputStream = BundlerTestHelper.class.getResourceAsStream(jarName)) {
                if (inputStream == null) {
                    throw new IllegalArgumentException("Resource not found: " + jarName);
                }

                String fileName = Paths.get(jarName).getFileName().toString();
                Path tempFile = Files.createTempFile("extracted-", "-" + fileName);
                tempFile.toFile().deleteOnExit();

                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

                return tempFile;
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to copy resource to temp file", e);
            }
        }).toList();
        return jars;
    }

}
