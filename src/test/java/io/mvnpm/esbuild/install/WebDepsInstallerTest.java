package io.mvnpm.esbuild.install;

import static io.mvnpm.esbuild.install.WebDepsInstaller.getMvnpmInfoPath;
import static io.mvnpm.esbuild.install.WebDepsInstaller.install;
import static io.mvnpm.esbuild.install.WebDepsInstaller.readMvnpmInfo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
    }

    @Test
    void testReInstall() throws IOException {
        Path tempDir = Files.createTempDirectory("testInstall");
        install(tempDir, getWebDependencies(List.of("/mvnpm/stimulus-3.2.1.jar", "/mvnpm/hooks-0.4.9.jar")));
        install(tempDir, getWebDependencies(List.of("/mvnpm/stimulus-3.2.1.jar", "/mvnpm/hooks-0.4.9.jar")));
        final MvnpmInfo mvnpmInfo = readMvnpmInfo(getMvnpmInfoPath(tempDir));
        assertEquals(2, mvnpmInfo.installed().size());
        assertEquals(mvnpmInfo.installed(),
                Set.of(new MvnpmInfo.InstalledDependency("org.something:stimulus-3.2.1", List.of("@hotwired/stimulus")),
                        new MvnpmInfo.InstalledDependency("org.something:hooks-0.4.9", List.of("@restart/hooks"))));
    }

    @Test
    void testInstallWithNewDeps() throws IOException {
        Path tempDir = Files.createTempDirectory("testInstall");
        install(tempDir, getWebDependencies(List.of("/mvnpm/stimulus-3.2.1.jar", "/mvnpm/hooks-0.4.9.jar")));
        install(tempDir, getWebDependencies(List.of("/mvnpm/react-bootstrap-2.7.4.jar", "/mvnpm/hooks-0.4.9.jar")));
        final MvnpmInfo mvnpmInfo = readMvnpmInfo(getMvnpmInfoPath(tempDir));
        assertEquals(2, mvnpmInfo.installed().size());
        assertEquals(mvnpmInfo.installed(),
                Set.of(new MvnpmInfo.InstalledDependency("org.something:react-bootstrap-2.7.4", List.of("react-bootstrap")),
                        new MvnpmInfo.InstalledDependency("org.something:hooks-0.4.9", List.of("@restart/hooks"))));
        assertFalse(Files.exists(tempDir.resolve("@hotwired/stimulus")));
        install(tempDir, getWebDependencies(List.of("/mvnpm/react-bootstrap-2.7.4.jar", "/mvnpm/hooks-0.4.9.jar",
                "/mvnpm/vaadin-webcomponents-24.1.6.jar")));
        final MvnpmInfo mvnpmInfo2 = readMvnpmInfo(getMvnpmInfoPath(tempDir));
        assertEquals(3, mvnpmInfo2.installed().size());
        final MvnpmInfo.InstalledDependency installedVaadin = mvnpmInfo2.installed().stream()
                .filter(installedDependency -> installedDependency.id().equals("org.something:vaadin-webcomponents-24.1.6"))
                .findFirst()
                .get();
        assertEquals(55, installedVaadin.dirs().size());
        install(tempDir, getWebDependencies(
                List.of("/mvnpm/react-bootstrap-2.7.4.jar", "/mvnpm/hooks-0.4.9.jar", "/mvnpm/moment-2.29.4-sources.jar")));
        final MvnpmInfo mvnpmInfo3 = readMvnpmInfo(getMvnpmInfoPath(tempDir));
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
    void testNoInfo() throws IOException {
        Path tempDir = Files.createTempDirectory("testInstall");
        install(tempDir, getWebDependencies(List.of("/mvnpm/stimulus-3.2.1.jar", "/mvnpm/hooks-0.4.9.jar")));
        Files.deleteIfExists(getMvnpmInfoPath(tempDir));
        install(tempDir, getWebDependencies(List.of("/mvnpm/react-bootstrap-2.7.4.jar", "/mvnpm/hooks-0.4.9.jar")));
        final MvnpmInfo mvnpmInfo = readMvnpmInfo(getMvnpmInfoPath(tempDir));
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
