package io.mvnpm.esbuild.install;

import static io.mvnpm.esbuild.install.WebDepsInstaller.getMvnpmInfoPath;
import static io.mvnpm.esbuild.install.WebDepsInstaller.install;
import static io.mvnpm.esbuild.install.WebDepsInstaller.readMvnpmInfo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.mvnpm.esbuild.model.WebDependency;

public class WebDepsInstallerTest {

    @Test
    void testInstall() throws IOException {
        Path tempDir = Files.createTempDirectory("testInstall");
        install(tempDir, getWebDependencies(List.of("/mvnpm/stimulus-3.2.1.jar", "/mvnpm/hooks-0.4.9.jar")));
        final MvnpmInfo mvnpmInfo = readMvnpmInfo(getMvnpmInfoPath(tempDir));
        assertEquals(2, mvnpmInfo.installed().size());
        assertEquals(mvnpmInfo.installed(),
                Set.of(new MvnpmInfo.InstalledDependency("org.something:stimulus-3.2.1", List.of("@hotwired/stimulus")),
                        new MvnpmInfo.InstalledDependency("org.something:hooks-0.4.9", List.of("@restart/hooks"))));
        checkNodeModulesDir(tempDir, mvnpmInfo);
    }

    private void checkNodeModulesDir(Path nodeModules, MvnpmInfo mvnpmInfo) {
        final List<String> dirs = mvnpmInfo.installed().stream().flatMap(i -> i.dirs().stream()).toList();
        for (String dir : dirs) {
            final Path packageJson = nodeModules.resolve(dir).resolve("package.json");
            assertTrue(packageJson.toFile().exists(), "package.json should exist in " + packageJson);
        }
    }

    @Test
    void testReInstall() throws IOException {
        Path tempDir = Files.createTempDirectory("testReInstall");
        install(tempDir, getWebDependencies(List.of("/mvnpm/stimulus-3.2.1.jar", "/mvnpm/hooks-0.4.9.jar")));
        install(tempDir, getWebDependencies(List.of("/mvnpm/stimulus-3.2.1.jar", "/mvnpm/hooks-0.4.9.jar")));
        final MvnpmInfo mvnpmInfo = readMvnpmInfo(getMvnpmInfoPath(tempDir));
        assertEquals(2, mvnpmInfo.installed().size());
        assertEquals(mvnpmInfo.installed(),
                Set.of(new MvnpmInfo.InstalledDependency("org.something:stimulus-3.2.1", List.of("@hotwired/stimulus")),
                        new MvnpmInfo.InstalledDependency("org.something:hooks-0.4.9", List.of("@restart/hooks"))));
        checkNodeModulesDir(tempDir, mvnpmInfo);
    }

    @Test
    void testNewVersion() throws IOException {
        Path tempDir = Files.createTempDirectory("testNewVersion");
        install(tempDir, getWebDependencies(List.of("/mvnpm/stimulus-3.2.1.jar", "/mvnpm/hooks-0.4.9.jar")));
        install(tempDir, getWebDependencies(List.of("/mvnpm/stimulus-3.2.0.jar", "/mvnpm/hooks-0.4.9.jar")));
        final MvnpmInfo mvnpmInfo = readMvnpmInfo(getMvnpmInfoPath(tempDir));
        assertEquals(2, mvnpmInfo.installed().size());
        assertEquals(mvnpmInfo.installed(),
                Set.of(new MvnpmInfo.InstalledDependency("org.something:stimulus-3.2.0", List.of("@hotwired/stimulus")),
                        new MvnpmInfo.InstalledDependency("org.something:hooks-0.4.9", List.of("@restart/hooks"))));
        checkNodeModulesDir(tempDir, mvnpmInfo);
    }

    @Test
    void testBuildPackage() throws IOException {
        Path tempDir = Files.createTempDirectory("testBuildPackage");
        install(tempDir, getWebDependencies(List.of("/mvnpm/lit-3.1.0.jar")));
        final MvnpmInfo mvnpmInfo = readMvnpmInfo(getMvnpmInfoPath(tempDir));
        assertEquals(1, mvnpmInfo.installed().size());
        checkNodeModulesDir(tempDir, mvnpmInfo);
        final Path index = tempDir.resolve("lit/index.d.ts");
        assertTrue(index.toFile().exists(), index + " exists");
        final Path decorator = tempDir.resolve("lit/decorators.d.ts");
        assertTrue(decorator.toFile().exists(), decorator + " exists");
    }

    @Test
    void testIncompatiblePackage() throws IOException {
        Path tempDir = Files.createTempDirectory("testIncompatiblePackage");
        assertThrowsExactly(InstallException.class,
                () -> install(tempDir, getWebDependencies(List.of("/mvnpm/jquery-3.7.1.jar"))));
    }

    @Test
    void testInstallWithNewDeps() throws IOException {
        Path tempDir = Files.createTempDirectory("testInstallWithNewDeps");
        install(tempDir, getWebDependencies(List.of("/mvnpm/stimulus-3.2.1.jar", "/mvnpm/hooks-0.4.9.jar")));
        install(tempDir, getWebDependencies(List.of("/mvnpm/react-bootstrap-2.7.4.jar", "/mvnpm/hooks-0.4.9.jar")));
        final MvnpmInfo mvnpmInfo = readMvnpmInfo(getMvnpmInfoPath(tempDir));
        checkNodeModulesDir(tempDir, mvnpmInfo);
        assertEquals(2, mvnpmInfo.installed().size());
        assertEquals(mvnpmInfo.installed(),
                Set.of(new MvnpmInfo.InstalledDependency("org.something:react-bootstrap-2.7.4", List.of("react-bootstrap")),
                        new MvnpmInfo.InstalledDependency("org.something:hooks-0.4.9", List.of("@restart/hooks"))));
        assertFalse(Files.exists(tempDir.resolve("@hotwired/stimulus")));
        install(tempDir, getWebDependencies(List.of("/mvnpm/react-bootstrap-2.7.4.jar", "/mvnpm/hooks-0.4.9.jar",
                "/mvnpm/vaadin-webcomponents-24.1.6.jar")));
        final MvnpmInfo mvnpmInfo2 = readMvnpmInfo(getMvnpmInfoPath(tempDir));
        checkNodeModulesDir(tempDir, mvnpmInfo2);
        assertEquals(3, mvnpmInfo2.installed().size());
        final MvnpmInfo.InstalledDependency installedVaadin = mvnpmInfo2.installed().stream()
                .filter(installedDependency -> installedDependency.id().equals("org.something:vaadin-webcomponents-24.1.6"))
                .findFirst()
                .get();
        assertEquals(55, installedVaadin.dirs().size());
        install(tempDir, getWebDependencies(
                List.of("/mvnpm/react-bootstrap-2.7.4.jar", "/mvnpm/hooks-0.4.9.jar", "/mvnpm/moment-2.29.4-sources.jar")));
        final MvnpmInfo mvnpmInfo3 = readMvnpmInfo(getMvnpmInfoPath(tempDir));
        checkNodeModulesDir(tempDir, mvnpmInfo3);
        assertEquals(3, mvnpmInfo2.installed().size());
        assertEquals(mvnpmInfo3.installed(),
                Set.of(new MvnpmInfo.InstalledDependency("org.something:react-bootstrap-2.7.4", List.of("react-bootstrap")),
                        new MvnpmInfo.InstalledDependency("org.something:hooks-0.4.9", List.of("@restart/hooks")),
                        new MvnpmInfo.InstalledDependency("org.something:moment-2.29.4-sources", List.of("moment"))));
        for (String dir : installedVaadin.dirs()) {
            assertFalse(Files.exists(tempDir.resolve(dir)));
        }
    }

    @Test
    void testVaadinInputError() throws IOException {
        Path tempDir = Files.createTempDirectory("testVaadinInputError");
        install(tempDir, getWebDependencies(List.of("/mvnpm/vaadin-webcomponents-24.3.5.jar")));
        final MvnpmInfo mvnpmInfo = readMvnpmInfo(getMvnpmInfoPath(tempDir));
        checkNodeModulesDir(tempDir, mvnpmInfo);
        assertEquals(1, mvnpmInfo.installed().size());
        final MvnpmInfo.InstalledDependency installedVaadin = mvnpmInfo.installed().stream()
                .filter(installedDependency -> installedDependency.id().equals("org.something:vaadin-webcomponents-24.3.5"))
                .findFirst()
                .get();
        assertEquals(55, installedVaadin.dirs().size());
    }

    @Test
    void testNoInfo() throws IOException {
        Path tempDir = Files.createTempDirectory("testNoInfo");
        install(tempDir, getWebDependencies(List.of("/mvnpm/stimulus-3.2.1.jar", "/mvnpm/hooks-0.4.9.jar")));
        Files.deleteIfExists(getMvnpmInfoPath(tempDir));
        install(tempDir, getWebDependencies(List.of("/mvnpm/react-bootstrap-2.7.4.jar", "/mvnpm/hooks-0.4.9.jar")));
        final MvnpmInfo mvnpmInfo = readMvnpmInfo(getMvnpmInfoPath(tempDir));
        checkNodeModulesDir(tempDir, mvnpmInfo);
        assertEquals(2, mvnpmInfo.installed().size());
        assertEquals(mvnpmInfo.installed(),
                Set.of(new MvnpmInfo.InstalledDependency("org.something:react-bootstrap-2.7.4", List.of("react-bootstrap")),
                        new MvnpmInfo.InstalledDependency("org.something:hooks-0.4.9", List.of("@restart/hooks"))));
    }

    private List<WebDependency> getWebDependencies(List<String> jarNames) {
        return jarNames.stream().map(jarName -> {
            try {
                return new File(getClass().getResource(jarName).toURI()).toPath();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }).map(d -> WebDependency.of("org.something:" + d.getFileName().toString().replace(".jar", ""), d,
                WebDependency.WebDependencyType.MVNPM)).toList();
    }
}
