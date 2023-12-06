package io.mvnpm.esbuild.install;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mvnpm.esbuild.model.WebDependency;
import io.mvnpm.esbuild.util.JarInspector;
import io.mvnpm.esbuild.util.PathUtils;
import io.mvnpm.esbuild.util.UnZip;

public final class WebDepsInstaller {

    private static final Logger logger = Logger.getLogger(WebDepsInstaller.class.getName());

    private static final String MVNPM_DIR = ".mvnpm";

    private static final ObjectMapper mapper = new ObjectMapper();

    public static boolean install(Path nodeModulesDir, List<WebDependency> dependencies) throws IOException {
        final Path mvnpmInfoFile = getMvnpmInfoPath(nodeModulesDir);
        final MvnpmInfo mvnpmInfo = readMvnpmInfo(mvnpmInfoFile);
        if (mvnpmInfo.installed().isEmpty() || dependencies.isEmpty()) {
            // Make sure it is clean
            PathUtils.deleteRecursive(nodeModulesDir);
        }
        if (dependencies.isEmpty()) {
            return true;
        }
        if (!Files.exists(nodeModulesDir)) {
            Files.createDirectories(nodeModulesDir);
        }
        final Path tmp = nodeModulesDir.resolve(MVNPM_DIR).resolve("tmp");
        final Set<MvnpmInfo.InstalledDependency> installed = new HashSet<>();
        boolean changed = false;
        for (WebDependency dep : dependencies) {
            final Optional<MvnpmInfo.InstalledDependency> alreadyInstalled = mvnpmInfo.installed().stream()
                    .filter(i -> i.id().equals(dep.id())).findFirst();
            if (alreadyInstalled.isPresent()) {
                logger.log(Level.FINE, "skipping package as it already exists ''{0}''", dep.id());
                installed.add(alreadyInstalled.get());
                continue;
            }
            changed = true;
            final Path extractDir = tmp.resolve(dep.id());
            PathUtils.deleteRecursive(extractDir);
            UnZip.unzip(dep.path(), extractDir);
            final Map<String, Path> packageNameAndRoot = JarInspector.findPackageNameAndRoot(extractDir, dep.type());
            List<String> dirs = new ArrayList<>();
            if (!packageNameAndRoot.isEmpty()) {
                for (Map.Entry<String, Path> nameAndRoot : packageNameAndRoot.entrySet()) {
                    final String packageName = nameAndRoot.getKey();
                    final Path source = nameAndRoot.getValue();
                    final Path target = nodeModulesDir.resolve(packageName);
                    dirs.add(packageName);
                    PathUtils.deleteRecursive(target);
                    Files.createDirectories(target.getParent());
                    Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                    logger.log(Level.FINE, "installed package ''{0}''", packageName);
                }
                installed.add(new MvnpmInfo.InstalledDependency(dep.id(), dirs));
                logger.log(Level.FINE, "installed dep ''{0}'' (''{1}'')", new Object[] { dep.path(), dep.id() });
            } else {
                logger.log(Level.WARNING, "package.json not found in dep: ''{0}'' (''{1}'')",
                        new Object[] { dep.path(), dep.id() });
            }
        }
        PathUtils.deleteRecursive(tmp);
        for (MvnpmInfo.InstalledDependency installedDependency : mvnpmInfo.installed()) {
            if (!installed.contains(installedDependency)) {
                changed = true;
                logger.log(Level.FINE, "removing package as it is not needed anymore ''{0}''", installedDependency.id());
                for (String dir : installedDependency.dirs()) {
                    PathUtils.deleteRecursive(nodeModulesDir.resolve(dir));
                }
            }
        }
        final MvnpmInfo newMvnpmInfo = new MvnpmInfo(installed);
        WebDepsInstaller.writeMvnpmInfo(mvnpmInfoFile, newMvnpmInfo);
        return changed;
    }

    public static Path getMvnpmInfoPath(Path nodeModulesDir) {
        return nodeModulesDir.resolve(MVNPM_DIR).resolve("mvnpm.json");
    }

    public static MvnpmInfo readMvnpmInfo(Path path) {
        if (!Files.exists(path)) {
            return new MvnpmInfo(Set.of());
        }
        try (InputStream s = Files.newInputStream(path)) {
            return mapper.readValue(s, MvnpmInfo.class);
        } catch (IOException e) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return new MvnpmInfo(Set.of());
        }
    }

    public static void writeMvnpmInfo(Path path, MvnpmInfo root) {
        try {
            Files.deleteIfExists(path);
            Files.createDirectories(path.getParent());
            mapper.writer(new DefaultPrettyPrinter()).writeValue(path.toFile(), root);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
