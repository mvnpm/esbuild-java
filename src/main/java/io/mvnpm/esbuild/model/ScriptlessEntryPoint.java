package io.mvnpm.esbuild.model;

import static io.mvnpm.esbuild.install.WebDepsInstaller.getMvnpmInfoPath;
import static io.mvnpm.esbuild.util.JarInspector.PACKAGE_JSON;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mvnpm.esbuild.install.MvnpmInfo;
import io.mvnpm.esbuild.install.WebDepsInstaller;

public class ScriptlessEntryPoint implements EntryPoint {
    private static final String[] FIELDS = new String[] { "name", "module", "main", "style", "sass" };

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Path nodeModulesDir;

    public ScriptlessEntryPoint(Path nodeModulesDir) {
        this.nodeModulesDir = nodeModulesDir;
    }

    @Override
    public Path process(Path workDir) {
        MvnpmInfo mvnpmInfo = WebDepsInstaller.readMvnpmInfo(getMvnpmInfoPath(nodeModulesDir));

        Stream<MvnpmInfo.InstalledDependency> dependencies = mvnpmInfo.installed().stream();
        Stream<Path> packageJsons = dependencies.flatMap(
                dependency -> dependency.dirs().stream().map(d -> nodeModulesDir.resolve(d).resolve(PACKAGE_JSON)))
                .filter(Files::exists);

        String entries = packageJsons.map(packageJson -> {
            Map<String, String> data = readPackage(packageJson);
            if (data.containsKey("sass") || data.containsKey("style")) {
                String value = data.containsKey("sass") ? data.get("sass") : data.get("style");
                return IMPORT.formatted(data.get("name") + "/" + value);
            }
            String entryType = data.containsKey("module") ? EXPORT : IMPORT;
            return entryType.formatted(data.get("name"));
        }).collect(Collectors.joining("\n"));

        Path entry = workDir.resolve("main.js");
        try {
            Files.writeString(entry, entries, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return entry;
    }

    private static Map<String, String> readPackage(Path path) {
        Map<String, String> contents = new HashMap<>(FIELDS.length);
        try {
            JsonNode object = objectMapper.readTree(path.toFile());
            for (String field : FIELDS) {
                if (object.has(field)) {
                    contents.put(field, object.get(field).asText());
                }
            }
            return contents;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static final String EXPORT = "export * from \"%s\";";
    private static final String IMPORT = "import \"%s\";";
}
