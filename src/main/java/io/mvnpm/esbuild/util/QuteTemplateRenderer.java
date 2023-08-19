package io.mvnpm.esbuild.util;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class QuteTemplateRenderer {

    public static String render(String templateName, Object data) {
        Engine engine = Engine.builder().addDefaults().build();
        try (InputStream inputStream = QuteTemplateRenderer.class.getResourceAsStream("/%s".formatted(templateName))) {
            if(inputStream == null) {
                throw new IOException("Template not found: " + templateName);
            }
            final String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            final Template parsed = engine.parse(template);
            return parsed.render(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
