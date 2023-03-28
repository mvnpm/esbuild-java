package ch.nerdin.esbuild.util;

import java.io.File;
import java.util.List;
import java.util.Map;

public class EntryPoint {

    public static String convert(List<File> entryPoints) {
        return QuteTemplateRenderer.render("entrypoint-template.js", Map.of("scripts", entryPoints.stream().map(file -> {
            final int index = file.getName().lastIndexOf(".");
            final String name = file.getName().substring(0, index);
            return Map.of("name", name.replaceAll("-", ""), "file", name);
        })));
    }
}
