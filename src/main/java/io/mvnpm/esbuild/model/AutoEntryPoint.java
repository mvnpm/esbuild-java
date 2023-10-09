package io.mvnpm.esbuild.model;

import static io.mvnpm.esbuild.util.Copy.copyEntries;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class AutoEntryPoint implements EntryPoint {
    private static final Set<String> SCRIPTS = Set.of("js", "ts", "jsx", "tsx", "mjs", "mts", "cjs", "cts");
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
        try (StringWriter sw = new StringWriter()) {
            for (String script : scripts) {
                final String fileName = Path.of(script).getFileName().toString();
                final int index = fileName.lastIndexOf(".");
                String name = fileName.substring(0, index);
                final String ext = fileName.substring(index + 1);
                final boolean isScript = SCRIPTS.contains(ext);
                String line;
                if (isScript) {
                    script = script.substring(0, script.lastIndexOf("."));
                    name = name.replaceAll("-", "");
                    line = IMPORT_WITH_FROM.formatted(name, script);
                } else {
                    line = IMPORT_WITHOUT_FROM.formatted(script);
                }
                sw.write(line);
                sw.write("\n");
            }
            return sw.toString();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static final String IMPORT_WITH_FROM = "import * as %s from \"./%s\";";
    private static final String IMPORT_WITHOUT_FROM = "import \"./%s\";";
}
