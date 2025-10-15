package io.mvnpm.esbuild;

import io.mvnpm.esbuild.model.BundleOptions;
import io.mvnpm.esbuild.model.BundleOptionsBuilder;
import io.mvnpm.esbuild.model.BundleResult;
import io.mvnpm.esbuild.model.WebDependency.WebDependencyType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BundlerTestHelper {

    public static void executeTest(List<String> jarNames, WebDependencyType type, String scriptName, boolean check)
            throws URISyntaxException, IOException {
        final BundleOptions bundleOptions = getBundleOptions(jarNames, type, scriptName).build();
        final BundleResult result = Bundler.bundle(bundleOptions, true);

        if (check) {
            assertTrue(result.dist().toFile().exists());
        }

    }


    public static BundleOptionsBuilder getBundleOptions(List<String> jarNames, WebDependencyType type, String scriptName)
            throws URISyntaxException, IOException {
        final List<Path> jars = getJars(jarNames);
        final Path tempDir = Files.createTempDirectory("esbuild-test");
        final BundleOptionsBuilder bundleOptionsBuilder = BundleOptions.builder().withDependencies(jars, type);
        if (scriptName != null) {
            Files.copy(new File(BundlerTestHelper.class.getResource("/" + scriptName).toURI()).toPath(), tempDir.resolve(scriptName));
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
