package io.mvnpm.esbuild.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class AutoEntryPointTest {

    @Test
    public void testScript() throws URISyntaxException, IOException {
        // given
        final Path workDir = Files.createTempDirectory("test");
        final Path rootDir = getRootScriptsDir();
        // when
        final AutoEntryPoint entry = new AutoEntryPoint(rootDir, "bundle",
                List.of("script1.js", "script2-test.js", "sub/sub.js"));
        String entryContents = readEntry(entry, workDir);

        // then
        assertEquals("""
                export * from "./script1";
                export * from "./script2-test";
                export * from "./sub/sub";""", entryContents);
    }

    @Test
    public void testCss() throws URISyntaxException, IOException {
        // given
        final Path tempDirectory = Files.createTempDirectory("test");
        final Path rootDir = getRootScriptsDir();

        // when
        final AutoEntryPoint entry = new AutoEntryPoint(rootDir, "name", List.of("style.css"));
        String entryContents = readEntry(entry, tempDirectory);

        // then
        assertEquals("import \"./style.css\";", entryContents);
    }

    private Path getRootScriptsDir() throws URISyntaxException {
        return new File(getClass().getResource("/multi/").toURI()).toPath();
    }

    private static String readEntry(AutoEntryPoint entry, Path tempDirectory) throws FileNotFoundException {
        final FileInputStream inputStream = new FileInputStream(entry.process(tempDirectory).toFile());
        return new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    }
}
