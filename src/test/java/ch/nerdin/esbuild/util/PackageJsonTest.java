package ch.nerdin.esbuild.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PackageJsonTest {

    @Test
    void findPackageMVNPMTest() throws URISyntaxException, IOException {
        final Path temp = Files.createTempDirectory("findPackageMVNPMTest");
        UnZip.unzip(new File(getClass().getResource("/mvnpm/react-bootstrap-2.7.4.jar").toURI()).toPath(), temp);
        final Optional<Path> packageJson = PackageJson.findPackageJson(temp);
        assertTrue(packageJson.isPresent());
        final String name = PackageJson.readPackageName(packageJson.get());
        assertEquals("react-bootstrap", name);
    }

    @Test
    void findPackageWebjarsTest() throws URISyntaxException, IOException {
        final Path temp = Files.createTempDirectory("findPackageWebjarsTest");
        UnZip.unzip(new File(getClass().getResource("/webjars/restart__hooks-0.4.9.jar").toURI()).toPath(), temp);
        final Optional<Path> packageJson = PackageJson.findPackageJson(temp);
        assertTrue(packageJson.isPresent());
        final String name = PackageJson.readPackageName(packageJson.get());
        assertEquals("@restart/hooks", name);
    }
}