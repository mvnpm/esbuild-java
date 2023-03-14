package io.quarkus.esbuild;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static io.quarkus.esbuild.BundleDependencies.BundleType.MVNPM;
import static io.quarkus.esbuild.BundleDependencies.BundleType.WEB_JAR;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class BundleDependenciesTest {

    @Test
    public void shouldBundleMvnpm() throws URISyntaxException, IOException {
        executeTest("/stimulus-3.2.1-mvnpm.jar", MVNPM, "/application-mvnpm.js");
    }

    @Test
    public void shouldBundle() throws URISyntaxException, IOException {
        executeTest("/htmx.org-1.8.4.jar", WEB_JAR, "/application-webjar.js");
    }

    private void executeTest(String jarName, BundleDependencies.BundleType type, String scriptName) throws URISyntaxException, IOException {
        final File jar = new File(getClass().getResource(jarName).toURI());

        final BundleDependencies bundleDependencies = new BundleDependencies();
        final Path path = bundleDependencies.extract(Collections.singletonList(jar.toPath()), type);

        final Path entry = new File(getClass().getResource(scriptName).toURI()).toPath();
        final Path target = path.resolve(entry.getFileName());
        Files.copy(entry, target, REPLACE_EXISTING);

        bundleDependencies.esBuild(target);
    }
}
