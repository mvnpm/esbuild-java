package io.mvnpm.esbuild.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mvnpm.esbuild.install.InstallException;
import io.mvnpm.esbuild.model.WebDependency;
import io.mvnpm.importmap.ImportsDataBinding;

public class JarInspector {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(JarInspector.class.getName());
    public static final String PACKAGE_JSON = "package.json";
    public static final String IMPORTMAP_JSON = "importmap.json";

    public static final String POM_PROPERTIES = "pom.properties";
    private static final String MAVEN_ROOT = "META-INF/maven";

    private static final String MVNPM_PACKAGING_VERSION_KEY = "mvnpm.packagingVersion";

    private static final Set<String> COMPATIBLE_MVNPM_PACKAGING_VERSIONS = Set.of("1");
    public static final String BUILD_ARCHIVE = "META-INF/.build.tgz";
    private static final Map<WebDependency.WebDependencyType, List<String>> PACKAGE_DIRS = Map.of(
            WebDependency.WebDependencyType.MVNPM, List.of("META-INF/resources/_static", ""),
            WebDependency.WebDependencyType.WEBJARS, List.of("META-INF/resources/webjars"));
    private static final List<String> MULTIPLE_GROUP_IDS = List.of("org.mvnpm.at.mvnpm"); // Group Ids that can contain
    // multiple package.jsons
    // TODO: Allow this to be
    // configured

    public static Path findMvnpmBuildArchive(Path dir) {
        final Path buildPackage = dir.resolve(BUILD_ARCHIVE);
        if (Files.exists(buildPackage)) {
            return buildPackage;
        }
        return null;
    }

    public static Map<String, Path> findPackageNameAndRoot(String id, Path extractDir, WebDependency.WebDependencyType type) {

        Path dir = getPackageRootPath(extractDir, type);

        if (dir == null) {
            return Map.of();
        }

        Properties properties = getProperties(id, extractDir, type);
        String groupId = properties.getProperty("groupId", "");
        boolean shouldDoMultiple = MULTIPLE_GROUP_IDS.contains(groupId);

        // First try package.json
        Map<String, Path> found = findPackageNameAndRootWithPackage(dir, shouldDoMultiple);

        // If this is mvnpm and we could not find package.json we can try another way
        if (found.isEmpty() && type.equals(WebDependency.WebDependencyType.MVNPM)) {
            found = findPackageNameAndRootWithImportMap(extractDir, properties);
        }

        return found;
    }

    private static Path getPackageRootPath(Path extractDir, WebDependency.WebDependencyType type) {
        if (!PACKAGE_DIRS.containsKey(type)) {
            throw new RuntimeException("Invalid BundleType: " + type);
        }
        for (String packageDir : PACKAGE_DIRS.get(type)) {
            Path dir = extractDir.resolve(packageDir);
            if (Files.isDirectory(dir)) {
                return dir;
            }
        }
        return null;
    }

    private static Properties getProperties(String id, Path extractDir, WebDependency.WebDependencyType type) {
        Properties properties = new Properties();

        if (type.equals(WebDependency.WebDependencyType.MVNPM)) { // Only mvnpm support composite
            properties = getPomProperties(extractDir);
        }

        final String mvnpmPackagingVersion = properties.getProperty(MVNPM_PACKAGING_VERSION_KEY);
        if (mvnpmPackagingVersion != null) {
            final String[] split = mvnpmPackagingVersion.split("\\.");
            if (split.length > 0 && !COMPATIBLE_MVNPM_PACKAGING_VERSIONS.contains(split[0])) {
                throw new InstallException(
                        "This version of esbuild-java is not compatible with this artifact packaging structure: " + id
                                + " (upgrade the version of esbuild-java or use a previous version of this package).",
                        id);
            }
        }

        return properties;
    }

    private static Map<String, Path> findPackageNameAndRootWithPackage(Path root, boolean shouldDoMultiple) {
        Map<String, Path> paths = new HashMap<>();

        List<Path> foundFiles = searchFiles(root, PACKAGE_JSON, shouldDoMultiple);
        for (Path path : foundFiles) {
            String packageName = readPackageName(path);
            paths.putIfAbsent(packageName, path.getParent());
        }

        return paths;
    }

    private static Map<String, Path> findPackageNameAndRootWithImportMap(Path root, Properties properties) {
        Optional<Path> importMapJson = searchFile(root, IMPORTMAP_JSON);
        if (importMapJson.isPresent()) {
            try {
                Path importMap = importMapJson.get();
                String json = new String(Files.readAllBytes(importMap));
                Map<String, String> imports = ImportsDataBinding.toImports(json).getImports();
                // Find the first directory one
                String packageName = readPackageName(properties);
                String artifactId = properties.getProperty("artifactId");
                String version = properties.getProperty("version");
                Path fullExtractedRoot = null;
                String fullExtractedMain = null;
                for (Map.Entry<String, String> ie : imports.entrySet()) {
                    if (ie.getKey().endsWith("/") && fullExtractedRoot == null) {
                        Path resources = importMap.getParent().resolve("resources");
                        Path packageRoot = Path.of(resources.toString(), toRelativeDir(ie.getValue(), artifactId));
                        fullExtractedRoot = root.resolve(packageRoot);
                    } else if (fullExtractedMain == null) {
                        fullExtractedMain = ie.getValue();
                    }
                }

                String main = toRelativeMain(fullExtractedRoot.toString(), fullExtractedMain);
                PackageJsonCreator.createPackageJson(fullExtractedRoot, packageName, version, main);
                return Map.of(packageName, fullExtractedRoot);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        return Map.of();
    }

    private static String toRelativeDir(String path, String artifactId) {
        if (!path.endsWith(artifactId + "/")) { // Could be in a sub folder
            path = path.substring(0, path.indexOf(artifactId) + artifactId.length() + 1);
        }
        return path;
    }

    private static String toRelativeMain(String root, String main) {
        root = root.substring(root.indexOf("_static"));
        Path rootPath = Paths.get(root);
        if (main.startsWith("/"))
            main = main.substring(1);
        return rootPath.relativize(Paths.get(main)).toString();
    }

    private static String readPackageName(Properties properties) {
        String groupId = properties.getProperty("groupId");
        if (groupId.equals("org.mvnpm")) {
            groupId = "";
        } else {
            groupId = groupId.substring(10); // Cut out org.mvnpm
            groupId = groupId.replaceFirst("at.", "@");
            groupId = groupId + "/";
        }
        return groupId + properties.getProperty("artifactId");
    }

    private static String readPackageName(Path path) {
        try {
            JsonNode object = objectMapper.readTree(path.toFile());
            return object.get("name").asText();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static Properties getPomProperties(Path extractDir) {
        Properties properties = new Properties();

        Path metaInfMavenDir = extractDir.resolve(MAVEN_ROOT);
        if (!Files.isDirectory(metaInfMavenDir)) {
            return properties;
        }
        Optional<Path> maybePomProperties = searchFile(extractDir.resolve(MAVEN_ROOT), POM_PROPERTIES);
        if (maybePomProperties.isPresent()) {
            Path pomProperties = maybePomProperties.get();
            try {
                properties.load(Files.newInputStream(pomProperties));
            } catch (IOException ex) {
                logger.log(Level.WARNING, "could not read properties ''{0}''", pomProperties);
            }
        }
        return properties;
    }

    /**
     * Find the first match recursively
     *
     * @param rootPath starting
     * @param targetFileName file we are looking for
     * @return the Path to the found file
     */
    private static Optional<Path> searchFile(Path rootPath, final String targetFileName) {
        List<Path> found = searchFiles(rootPath, targetFileName, false);
        if (!found.isEmpty()) {
            return Optional.of(found.get(0));
        }
        return Optional.empty();
    }

    private static List<Path> searchFiles(Path rootPath, final String targetFileName, boolean multiple) {
        List<Path> found = new ArrayList<>();
        Queue<Path> queue = new LinkedList<>();
        queue.add(rootPath);

        while (!queue.isEmpty()) {
            Path currentPath = queue.poll();

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentPath)) {
                for (Path entry : stream) {
                    if (Files.isDirectory(entry)) {
                        queue.add(entry);
                    } else if (entry.getFileName().toString().equals(targetFileName)) {
                        found.add(entry);
                        if (!multiple)
                            return found;
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        return found;
    }
}
