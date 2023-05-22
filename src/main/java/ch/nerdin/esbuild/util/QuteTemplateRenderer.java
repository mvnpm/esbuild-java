package ch.nerdin.esbuild.util;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class QuteTemplateRenderer {

    public static String render(String templateName, Object data) {
        Engine engine = Engine.builder().addDefaults().build();
        final InputStream inputStream = QuteTemplateRenderer.class.getResourceAsStream("/%s".formatted(templateName));
        String template = new BufferedReader(
            new InputStreamReader(inputStream, StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining("\n"));

        final Template parsed = engine.parse(template);
        return parsed.render(data);
    }
}
