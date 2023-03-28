package ch.nerdin.esbuild.util;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

public class QuteTemplateRenderer {

    public static String render(String templateName, Object data) {
        Engine engine = Engine.builder().addDefaults().build();
        final URL location = ImportToPackage.class.getResource("/%s".formatted(templateName));
        final List<String> template;
        try {
            template = Files.readAllLines(new File(location.toURI()).toPath());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        final Template parsed = engine.parse(String.join("\n", template));
        return parsed.render(data);
    }
}
