package ch.nerdin.esbuild.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PackageJsonTest {

    @Test
    void findPackageMVNPMTest() throws URISyntaxException {
        final Optional<Path> packageJson = PackageJson.findPackageJson(new File(getClass().getResource("/mvnpm/react-bootstrap").toURI()).toPath());
        assertTrue(packageJson.isPresent());
        final String name = PackageJson.readPackageName(packageJson.get());
        assertEquals("react-bootstrap", name);
    }

    @Test
    void findPackageWebjarsTest() throws URISyntaxException {
        final Optional<Path> packageJson = PackageJson.findPackageJson(new File(getClass().getResource("/webjars/restart__hooks-0.4.9").toURI()).toPath());
        assertTrue(packageJson.isPresent());
        final String name = PackageJson.readPackageName(packageJson.get());
        assertEquals("@restart/hooks", name);
    }
}