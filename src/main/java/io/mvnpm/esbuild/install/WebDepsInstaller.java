package io.mvnpm.esbuild.install;

import static io.mvnpm.esbuild.util.JarInspector.findMvnpmMoreArchive;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mvnpm.esbuild.model.WebDependency;
import io.mvnpm.esbuild.util.Archives;
import io.mvnpm.esbuild.util.JarInspector;
import io.mvnpm.esbuild.util.PathUtils;

public final class WebDepsInstaller {

    private static final Logger logger = Logger.getLogger(WebDepsInstaller.class.getName());

    private static final String MVNPM_DIR = ".mvnpm";

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void install(Path nodeModulesDir, List<WebDependency> dependencies) throws IOException {
        final Path mvnpmInfoFile = getMvnpmInfoPath(nodeModulesDir);
        final MvnpmInfo mvnpmInfo = readMvnpmInfo(mvnpmInfoFile);
        if (mvnpmInfo.installed().isEmpty() || dependencies.isEmpty()) {
            // Make sure it is clean
            PathUtils.deleteRecursive(nodeModulesDir);
        }
        if (dependencies.isEmpty())
            return;
        if (!Files.exists(nodeModulesDir)) {
            Files.createDirectories(nodeModulesDir);
        }
        final Set<MvnpmInfo.InstalledDependency> installed = new HashSet<>();

        for (MvnpmInfo.InstalledDependency i : mvnpmInfo.installed()) {
            if (dependencies.stream().map(WebDependency::id).anyMatch(id -> id.equals(i.id()))) {
                logger.log(Level.FINE, "Skipping package ''{0}''", i.id());
                installed.add(i);
            } else {
                for (String legacyDir : i.dirs()) {
                    logger.log(Level.FINE, "removing package as it is not needed anymore ''{0}''", legacyDir);
                    PathUtils.deleteRecursive(nodeModulesDir.resolve(legacyDir));
                }
            }
        }

        Stream<WebDependency> installableDependencies = dependencies.stream()
                .filter(d -> mvnpmInfo.installed().stream()
                        .map(MvnpmInfo.InstalledDependency::id)
                        .noneMatch(id -> id.equals(d.id())));

        installDependencies(nodeModulesDir, installableDependencies, installed);
        final MvnpmInfo newMvnpmInfo = new MvnpmInfo(installed);
        WebDepsInstaller.writeMvnpmInfo(mvnpmInfoFile, newMvnpmInfo);
    }

    private static void installDependencies(Path nodeModulesDir, Stream<WebDependency> installableDependencies,
            Set<MvnpmInfo.InstalledDependency> installed) throws IOException {
        final Path tmp = nodeModulesDir.resolve(MVNPM_DIR).resolve("tmp");
        installableDependencies.forEach(dep -> {
            try {
                final Path extractDir = tmp.resolve(dep.id().replace(":", "/"));
                PathUtils.deleteRecursive(extractDir);
                Archives.unzip(dep.path(), extractDir);
                if (dep.type() == WebDependency.WebDependencyType.MVNPM) {
                    final Path mvnpmMoreArchive = findMvnpmMoreArchive(extractDir);
                    if (mvnpmMoreArchive != null) {
                        logger.log(Level.FINE, "Found more archive ''{0}''", mvnpmMoreArchive);
                        try {
                            Archives.unTgz(mvnpmMoreArchive, mvnpmMoreArchive.getParent());
                        } catch (IOException e) {
                            logger.log(Level.WARNING, "Could not extract .more.tgz archive '" + mvnpmMoreArchive + "'", e);
                        }
                    }
                }
                final Map<String, Path> packageNameAndRoot = JarInspector.findPackageNameAndRoot(dep.id(), extractDir,
                        dep.type());
                List<String> dirs = new ArrayList<>();
                if (!packageNameAndRoot.isEmpty()) {
                    for (Map.Entry<String, Path> nameAndRoot : packageNameAndRoot.entrySet()) {
                        final String packageName = nameAndRoot.getKey();
                        final Path source = nameAndRoot.getValue();
                        final Path target = nodeModulesDir.resolve(packageName);
                        dirs.add(packageName);
                        PathUtils.deleteRecursive(target);
                        Files.createDirectories(target.getParent());
                        PathUtils.safeMove(source, target);
                        logger.log(Level.FINE, "installed package ''{0}''", packageName);
                    }
                    installed.add(new MvnpmInfo.InstalledDependency(dep.id(), dirs));
                    logger.log(Level.FINE, "installed dep ''{0}'' (''{1}'')", new Object[] { dep.path(), dep.id() });
                } else {
                    logger.log(Level.WARNING, "package.json not found in dep: ''{0}'' (''{1}'')",
                            new Object[] { dep.path(), dep.id() });
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        PathUtils.deleteRecursive(tmp);
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
