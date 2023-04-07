package ch.nerdin.esbuild.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntryPoint {

    private static final Set<String> SCRIPTS = Set.of("js", "ts", "jsx", "tsx");

    public static String convert(List<String> entryPoints) {
        return QuteTemplateRenderer.render("entrypoint-template.js", Map.of("imports", entryPoints.stream().map(fileName -> {
            final int index = fileName.lastIndexOf(".");
            final String name = fileName.substring(0, index);
            final String ext = fileName.substring(index + 1);
            final boolean isScript = SCRIPTS.contains(ext);
            final Map<String, String> imports = new HashMap<>();
            imports.put("from", isScript ? name : fileName);
            imports.put("as", isScript ? name.replaceAll("-", "") : null);
            return imports;
        })));
    }
}
