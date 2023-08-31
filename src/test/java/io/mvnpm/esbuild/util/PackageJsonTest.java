package io.mvnpm.esbuild.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PackageJsonTest {

    @Test
    void findPackageMVNPMTest() throws URISyntaxException, IOException {
        final Path temp = Files.createTempDirectory("findPackageMVNPMTest");
        UnZip.unzip(new File(getClass().getResource("/mvnpm/react-bootstrap-2.7.4.jar").toURI()).toPath(), temp);
        final List<Path> packageJson = JarInspector.findPackageJsons(temp, false);
        assertFalse(packageJson.isEmpty());
        final String name = JarInspector.readPackageName(packageJson.get(0));
        assertEquals("react-bootstrap", name);
    }

    @Test
    void findPackageMVNPMCompositeTest() throws URISyntaxException, IOException {
        final Path temp = Files.createTempDirectory("findPackageMVNPMCompositeTest");
        UnZip.unzip(new File(getClass().getResource("/mvnpm/vaadin-webcomponents-24.1.6.jar").toURI()).toPath(), temp);
        final List<Path> packageJsons = JarInspector.findPackageJsons(temp, true);
        assertFalse(packageJsons.isEmpty());
        assertTrue(packageJsons.size()>1);
    }
    
    @Test
    void findPackageWebjarsTest() throws URISyntaxException, IOException {
        final Path temp = Files.createTempDirectory("findPackageWebjarsTest");
        UnZip.unzip(new File(getClass().getResource("/webjars/restart__hooks-0.4.9.jar").toURI()).toPath(), temp);
        final List<Path> packageJson = JarInspector.findPackageJsons(temp, false);
        assertFalse(packageJson.isEmpty());
        final String name = JarInspector.readPackageName(packageJson.get(0));
        assertEquals("@restart/hooks", name);
    }
}