package io.mvnpm.esbuild.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArchivesTest {

    @Test
    public void unzip() throws URISyntaxException, IOException {
        // given
        final File jarFile = new File(this.getClass().getResource("/mvnpm/stimulus-3.2.1.jar").toURI());
        final Path temp = Files.createTempDirectory("unzip");

        // when
        Archives.unzip(jarFile.toPath(), temp);

        // then
        final String[] list = temp.toFile().list();
        assertArrayEquals(new String[] { "META-INF" }, list);

    }

    @Test
    public void unTgz() throws URISyntaxException, IOException {
        // given
        final File jarFile = new File(this.getClass().getResource("/mvnpm/lit-3.1.0.jar").toURI());
        final Path temp = Files.createTempDirectory("unTgz");

        // when
        Archives.unzip(jarFile.toPath(), temp);
        Path mvnpmBuildPackage = JarInspector.findMvnpmBuildArchive(temp);
        Assertions.assertNotNull(mvnpmBuildPackage);
        Archives.unTgz(mvnpmBuildPackage, mvnpmBuildPackage.getParent());

        // then
        assertTrue(temp.resolve("META-INF/resources/_static/lit/3.1.0/index.d.ts").toFile().exists());
        assertTrue(temp.resolve("META-INF/resources/_static/lit/3.1.0/index.js").toFile().exists());
        assertTrue(temp.resolve("META-INF/resources/_static/lit/3.1.0/decorators.d.ts").toFile().exists());
        assertTrue(temp.resolve("META-INF/resources/_static/lit/3.1.0/decorators/custom-element.d.ts").toFile().exists());
        assertTrue(temp.resolve("META-INF/resources/_static/lit/3.1.0/decorators/custom-element.js").toFile().exists());
    }
}
