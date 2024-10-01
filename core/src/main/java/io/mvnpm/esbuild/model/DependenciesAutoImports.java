package io.mvnpm.esbuild.model;

import static io.mvnpm.esbuild.install.WebDepsInstaller.getMvnpmInfoPath;
import static io.mvnpm.esbuild.model.AutoEntryPoint.isScript;
import static io.mvnpm.esbuild.util.JarInspector.PACKAGE_JSON;
import static java.util.function.Predicate.not;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mvnpm.esbuild.install.MvnpmInfo;
import io.mvnpm.esbuild.install.WebDepsInstaller;

public class DependenciesAutoImports {
    private static final String[] FIELDS = new String[] { "name", "module", "main", "style", "sass", "browser" };

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static String dependenciesImports(Path nodeModulesDir, Predicate<String> idsPredicate, boolean onlyStyles) {
        MvnpmInfo mvnpmInfo = WebDepsInstaller.readMvnpmInfo(getMvnpmInfoPath(nodeModulesDir));

        Stream<Path> packageJsons = mvnpmInfo.installed().stream()
                .filter(d -> idsPredicate.test(d.id()))
                .flatMap(dependency -> dependency.dirs().stream().map(d -> nodeModulesDir.resolve(d).resolve(PACKAGE_JSON)))
                .filter(Files::exists);

        String entries = packageJsons.map(packageJson -> {
            StringBuilder imports = new StringBuilder();
            Map<String, String> data = readPackage(packageJson);
            // Some packages have both style and script, so we need to check both
            if (!data.getOrDefault("sass", "").isBlank()) {
                imports.append(IMPORT.formatted(data.get("name") + "/" + data.get("sass"))).append("\n");
            } else if (!data.getOrDefault("style", "").isBlank()) {
                imports.append(IMPORT.formatted(data.get("name") + "/" + data.get("style"))).append("\n");
            }
            if (onlyStyles) {
                return imports.toString();
            }
            // Based on this: https://esbuild.github.io/api/#platform
            // Use module if browser is defined else use main if defined
            if (isScript(data.get("module")) && !data.getOrDefault("browser", "").isBlank()) {
                imports.append(IMPORT.formatted(data.get("name"))).append("\n");
            } else if (isScript(data.get("main"))) {
                imports.append(IMPORT.formatted(data.get("name"))).append("\n");
            }
            return imports.toString();
        }).filter(not(String::isBlank)).collect(Collectors.joining(""));
        return "// Auto-generated imports for web dependencies\n" + entries + "\n";
    }

    private static Map<String, String> readPackage(Path path) {
        Map<String, String> contents = new HashMap<>(FIELDS.length);
        try {
            JsonNode object = objectMapper.readTree(path.toFile());
            for (String field : FIELDS) {
                if (object.has(field)) {
                    final JsonNode node = object.get(field);
                    contents.put(field, node.isValueNode() ? node.asText() : "[object]");
                }
            }
            return contents;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static final String IMPORT = "import \"%s\";";
}
