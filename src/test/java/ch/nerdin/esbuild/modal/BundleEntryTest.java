package ch.nerdin.esbuild.modal;

import org.junit.jupiter.api.Test;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BundleEntryTest {

    @Test
    public void testScript() throws URISyntaxException, IOException {
        // given
        final Path tempDirectory = Files.createTempDirectory("test");
        final Path script1 = createTempScript("script1.js");
        final Path script2 = createTempScript("script2-test.js");

        // when
        final BundleEntry entry = new BundleEntry("bundle", List.of(script1, script2));
        String entryContents = readEntry(entry, tempDirectory);

        // then
        assertEquals("""
                import * as script1 from "./script1";
                import * as script2test from "./script2-test";""", entryContents);
    }

    @Test
    public void testCss() throws URISyntaxException, IOException {
        // given
        final Path tempDirectory = Files.createTempDirectory("test");
        final Path css = createTempScript("style.css");

        // when
        final BundleEntry entry = new BundleEntry("name", List.of(css));
        String entryContents = readEntry(entry, tempDirectory);

        // then
        assertEquals("import \"./style.css\";", entryContents);
    }

    private Path createTempScript(String name) throws URISyntaxException {
        return new File(getClass().getResource("/multi/%s".formatted(name)).toURI()).toPath();
    }

    private static String readEntry(BundleEntry entry, Path tempDirectory) throws FileNotFoundException {
        final FileInputStream inputStream = new FileInputStream(entry.process(tempDirectory).toFile());
        return new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    }
}
