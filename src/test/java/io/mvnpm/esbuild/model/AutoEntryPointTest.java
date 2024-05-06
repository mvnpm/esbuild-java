package io.mvnpm.esbuild.model;

import static io.mvnpm.esbuild.install.WebDepsInstaller.install;
import static io.mvnpm.esbuild.install.WebDepsInstallerTest.getWebDependencies;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.mvnpm.esbuild.model.AutoEntryPoint.AutoDeps;
import io.mvnpm.esbuild.model.AutoEntryPoint.AutoDepsMode;

public class AutoEntryPointTest {

    @Test
    public void testScript() throws URISyntaxException, IOException {
        // given
        final Path workDir = Files.createTempDirectory("test");
        final Path rootDir = getRootScriptsDir();
        // when
        final EntryPoint entry = AutoEntryPoint.withoutAutoDeps(rootDir, "bundle",
                List.of("script1.js", "script2-test.js", "sub/sub.js"));
        String entryContents = readEntry(entry, workDir);

        // then
        assertEquals("""
                // Auto-generated imports for project sources
                export * from "./script1";
                export * from "./script2-test";
                export * from "./sub/sub";
                """, entryContents);
    }

    @Test
    public void testCss() throws URISyntaxException, IOException {
        // given
        final Path tempDirectory = Files.createTempDirectory("test");
        final Path rootDir = getRootScriptsDir();

        // when
        final EntryPoint entry = AutoEntryPoint.withoutAutoDeps(rootDir, "name", List.of("style.css"));
        String entryContents = readEntry(entry, tempDirectory);

        // then
        assertEquals("""
                // Auto-generated imports for project sources
                import "./style.css";
                """, entryContents);
    }

    @Test
    public void testCssAndDeps() throws URISyntaxException, IOException {
        // given
        final Path tempDirectory = Files.createTempDirectory("test");
        final Path rootDir = getRootScriptsDir();

        final Path nodeModules = tempDirectory.resolve("node_modules");
        install(nodeModules, getWebDependencies(List.of("/mvnpm/bootstrap-5.2.3.jar", "/mvnpm/stimulus-3.2.1.jar")));

        // when
        final EntryPoint entry = AutoEntryPoint.withAutoDeps(rootDir, "name", List.of("style.css"),
                new AutoDeps(AutoDepsMode.AUTO,
                        nodeModules));
        String entryContents = readEntry(entry, tempDirectory);

        // then
        assertEquals("""
                // Auto-generated imports for project sources
                import "./style.css";

                // Auto-generated imports for web dependencies
                import "bootstrap/scss/bootstrap.scss";
                import "bootstrap";
                import "@hotwired/stimulus";
                """, entryContents);
    }

    @Test
    public void testCssAndScript() throws URISyntaxException, IOException {
        // given
        final Path tempDirectory = Files.createTempDirectory("test");
        final Path rootDir = getRootScriptsDir();

        final Path nodeModules = tempDirectory.resolve("node_modules");
        install(nodeModules, getWebDependencies(List.of("/mvnpm/bootstrap-5.2.3.jar", "/mvnpm/stimulus-3.2.1.jar")));

        // when
        final EntryPoint entry = AutoEntryPoint.withAutoDeps(rootDir, "name", List.of("style.css", "script1.js"),
                new AutoDeps(AutoDepsMode.AUTO, nodeModules));
        String entryContents = readEntry(entry, tempDirectory);

        // then
        assertEquals("""
                // Auto-generated imports for project sources
                import "./style.css";
                export * from "./script1";

                // Auto-generated imports for web dependencies
                import "bootstrap/scss/bootstrap.scss";
                """, entryContents);
    }

    @Test
    public void testCssAndScriptAndAutoDeps() throws URISyntaxException, IOException {
        // given
        final Path tempDirectory = Files.createTempDirectory("test");
        final Path rootDir = getRootScriptsDir();

        final Path nodeModules = tempDirectory.resolve("node_modules");
        install(nodeModules, getWebDependencies(List.of("/mvnpm/bootstrap-5.2.3.jar", "/mvnpm/stimulus-3.2.1.jar")));

        // when
        final EntryPoint entry = AutoEntryPoint.withAutoDeps(rootDir, "name", List.of("style.css", "script1.js"),
                new AutoDeps(AutoDepsMode.ALL, nodeModules));
        String entryContents = readEntry(entry, tempDirectory);

        // then
        assertEquals("""
                // Auto-generated imports for project sources
                import "./style.css";
                export * from "./script1";

                // Auto-generated imports for web dependencies
                import "bootstrap/scss/bootstrap.scss";
                import "bootstrap";
                import "@hotwired/stimulus";
                """, entryContents);
    }

    private Path getRootScriptsDir() throws URISyntaxException {
        return new File(getClass().getResource("/multi/").toURI()).toPath();
    }

    private static String readEntry(EntryPoint entry, Path tempDirectory) throws FileNotFoundException {
        final FileInputStream inputStream = new FileInputStream(entry.process(tempDirectory).toFile());
        return new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    }
}
