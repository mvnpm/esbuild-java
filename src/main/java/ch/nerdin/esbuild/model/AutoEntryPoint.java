package ch.nerdin.esbuild.model;

import ch.nerdin.esbuild.util.QuteTemplateRenderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static ch.nerdin.esbuild.util.Copy.copyEntries;

public class AutoEntryPoint implements EntryPoint {
    private static final Set<String> SCRIPTS = Set.of("js", "ts", "jsx", "tsx");
    private final String name;
    private final Path rootDir;
    private final List<String> scripts;

    public AutoEntryPoint(Path rootDir, String name, List<String> scripts) {
        this.name = name;
        this.rootDir = rootDir;
        this.scripts = scripts;
    }


    @Override
    public Path process(Path workDir) {
        try {
            if (!Objects.equals(rootDir, workDir)) {
                copyEntries(rootDir, scripts, workDir);
            }
            return bundleScripts(workDir, name, scripts);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path bundleScripts(Path workDir, String bundleName, List<String> scripts) throws IOException {
        final String entryString = convert(workDir, scripts);
        final Path entry = workDir.resolve("%s.js".formatted(bundleName));
        Files.writeString(entry, entryString, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return entry;
    }

    private String convert(Path workDir, List<String> scripts) {
        return QuteTemplateRenderer.render("entrypoint-template.js", Map.of("imports", scripts.stream().map(path -> {
            final String fileName = Path.of(path).getFileName().toString();
            final int index = fileName.lastIndexOf(".");
            final String name = fileName.substring(0, index);
            final String ext = fileName.substring(index + 1);
            final boolean isScript = SCRIPTS.contains(ext);
            final Map<String, String> imports = new HashMap<>();
            imports.put("from", isScript ? path.substring(0, path.lastIndexOf(".")) : path);
            imports.put("as", isScript ? name.replaceAll("-", "") : null);
            return imports;
        })));
    }
}
