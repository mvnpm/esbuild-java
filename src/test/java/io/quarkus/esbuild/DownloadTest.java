package io.quarkus.esbuild;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DownloadTest {
    public static String VERSION = "0.17.10";

    @Test
    public void shouldDownloadFile() throws IOException {
        final Path path = new Download(VERSION).execute();

        assertTrue(path.toFile().exists());
    }


}
