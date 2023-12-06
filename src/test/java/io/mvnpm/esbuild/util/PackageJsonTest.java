package io.mvnpm.esbuild.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.mvnpm.esbuild.model.WebDependency;

class PackageJsonTest {

    @Test
    void findPackageMVNPMTest() throws URISyntaxException, IOException {
        final Path temp = Files.createTempDirectory("findPackageMVNPMTest");
        UnZip.unzip(new File(getClass().getResource("/mvnpm/react-bootstrap-2.7.4.jar").toURI()).toPath(), temp);
        final Map<String, Path> packageNameAndRoot = JarInspector.findPackageNameAndRoot(temp,
                WebDependency.WebDependencyType.MVNPM);
        assertFalse(packageNameAndRoot.isEmpty());
        Map.Entry<String, Path> next = packageNameAndRoot.entrySet().iterator().next();
        final String name = next.getKey();
        assertEquals("react-bootstrap", name);
    }

    @Test
    void findPackageMVNPMCompositeTest() throws URISyntaxException, IOException {
        final Path temp = Files.createTempDirectory("findPackageMVNPMCompositeTest");
        UnZip.unzip(new File(getClass().getResource("/mvnpm/vaadin-webcomponents-24.1.6.jar").toURI()).toPath(), temp);
        final Map<String, Path> packageNameAndRoot = JarInspector.findPackageNameAndRoot(temp,
                WebDependency.WebDependencyType.MVNPM);
        assertFalse(packageNameAndRoot.isEmpty());
        assertTrue(packageNameAndRoot.size() > 1);
    }

    @Test
    void findPackageWebjarsTest() throws URISyntaxException, IOException {
        final Path temp = Files.createTempDirectory("findPackageWebjarsTest");
        UnZip.unzip(new File(getClass().getResource("/webjars/restart__hooks-0.4.9.jar").toURI()).toPath(), temp);
        final Map<String, Path> packageNameAndRoot = JarInspector.findPackageNameAndRoot(temp,
                WebDependency.WebDependencyType.WEBJARS);
        assertFalse(packageNameAndRoot.isEmpty());
        Map.Entry<String, Path> next = packageNameAndRoot.entrySet().iterator().next();
        final String name = next.getKey();
        assertEquals("@restart/hooks", name);
    }
}
