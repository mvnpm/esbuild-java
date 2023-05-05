package ch.nerdin.esbuild.modal;

import ch.nerdin.esbuild.util.QuteTemplateRenderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BundleEntry extends Entry {
    private static final Set<String> SCRIPTS = Set.of("js", "ts", "jsx", "tsx");
    private final String name;
    private final List<Path> scripts;

    public BundleEntry(String name, List<Path> scripts) {
        this.name = name;
        this.scripts = scripts;
    }

    private Path bundleScripts(String bundleName, List<Path> resources, Path location) throws IOException {
        final String entryString = convert(resources);
        final Path entry = location.resolve("%s.js".formatted(bundleName));
        Files.writeString(entry, entryString);
        return entry;
    }

    private String convert(List<Path> resources) {
        return QuteTemplateRenderer.render("entrypoint-template.js", Map.of("imports", resources.stream().map(path -> {
            final String fileName = path.getFileName().toString();
            final int index = fileName.lastIndexOf(".");
            final String name = fileName.substring(0, index);
            final String ext = fileName.substring(index + 1);
            final boolean isScript = SCRIPTS.contains(ext);
            final Map<String, String> imports = new HashMap<>();
            final String pathName = path.toString();
            imports.put("from", isScript ? pathName.substring(0, pathName.lastIndexOf(".")) : fileName);
            imports.put("as", isScript ? name.replaceAll("-", "") : null);
            return imports;
        })));
    }

    @Override
    public Path process(Path location) {
        try {
            return bundleScripts(name, copyToLocation(location, scripts), location);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
