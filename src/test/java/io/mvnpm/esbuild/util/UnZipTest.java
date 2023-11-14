package io.mvnpm.esbuild.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class UnZipTest {

    @Test
    public void unzip() throws URISyntaxException, IOException {
        // given
        final File jarFile = new File(this.getClass().getResource("/mvnpm/stimulus-3.2.1.jar").toURI());
        final Path junit = Files.createTempDirectory("junit");

        // when
        UnZip.unzip(jarFile.toPath(), junit);

        // then
        final String[] list = junit.toFile().list();
        assertArrayEquals(new String[] { "META-INF" }, list);

    }
}
