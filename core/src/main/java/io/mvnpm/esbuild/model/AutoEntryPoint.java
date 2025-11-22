package io.mvnpm.esbuild.model;

import static io.mvnpm.esbuild.util.PathUtils.copyEntries;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public record AutoEntryPoint(Path rootDir, String name, List<Source> sources, AutoDeps autoDeps) implements EntryPoint {

    private static final Set<String> SCRIPTS = Set.of("js", "ts", "jsx", "tsx", "mjs", "mts", "cjs", "cts");

    public AutoEntryPoint {
        requireNonNull(name, "name is required");
        requireNonNull(rootDir, "rootDir is required");
        requireNonNull(sources, "sources are required");
    }

    public static EntryPoint withoutAutoDeps(Path rootDir, String name, List<String> sources) {
        return withAutoDeps(rootDir, name, sources, null);
    }

    public static EntryPoint withAutoDeps(Path rootDir, String name, List<String> sources, AutoDeps autoDeps) {
        final List<Source> sourceList = requireNonNull(sources, "sources are required").stream().map(Source::of).toList();
        return new AutoEntryPoint(rootDir, name, sourceList, autoDeps);
    }

    @Override
    public Path process(Path workDir) {
        try {
            String content = "";
            AutoDepsMode resolvedMode = autoDeps != null ? autoDeps.mode() : AutoDepsMode.NONE;
            if (resolvedMode == AutoDepsMode.AUTO) {
                resolvedMode = sources.stream().noneMatch(Source::isScript) ? AutoDepsMode.ALL : AutoDepsMode.STYLES;
            }
            switch (resolvedMode) {
                case ALL -> content += DependenciesAutoImports.dependenciesImports(autoDeps.nodeModulesDir,
                        autoDeps.idsPredicate, false);
                case STYLES -> content += DependenciesAutoImports.dependenciesImports(autoDeps.nodeModulesDir,
                        autoDeps.idsPredicate, true);
                case NONE -> {
                }
            }
            content += autoImportsSources(workDir);
            return createEntryPoint(name, workDir, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Path createEntryPoint(String name, Path workDir, String content) throws IOException {
        final Path entry = workDir.resolve("%s.js".formatted(name));
        Files.writeString(entry, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return entry;
    }

    private String autoImportsSources(Path workDir) {
        if (sources.isEmpty()) {
            return "";
        }
        if (!Objects.equals(rootDir, workDir)) {
            copyEntries(rootDir, sources.stream().map(Source::relativePath).toList(), workDir);
        }
        try (StringWriter sw = new StringWriter()) {
            sw.write("// Auto-generated imports for project sources\n");
            for (Source source : sources) {
                if (source.isIgnored()) {
                    continue;
                }
                String line;
                if (source.isScript()) {
                    line = EXPORT.formatted(source.relativePathWithoutExt());
                } else {
                    line = IMPORT.formatted(source.relativePath());
                }
                sw.write(line);
                sw.write("\n");
            }
            sw.write("\n");
            return sw.toString();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static final String EXPORT = "export * from \"./%s\";";
    private static final String IMPORT = "import \"./%s\";";

    record Source(String relativePath, String ext) {
        public static Source of(String relativePath) {
            final String ext = resolveExtension(relativePath);
            return new Source(relativePath, ext);
        }

        public String relativePathWithoutExt() {
            return relativePath.substring(0, relativePath.lastIndexOf("."));
        }

        public boolean isIgnored() {
            return relativePath.contains("/_") || relativePath.startsWith("_");
        }

        public boolean isScript() {
            return SCRIPTS.contains(ext);
        }
    }

    public static boolean isScript(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return false;
        }
        final String ext = resolveExtension(relativePath);
        return SCRIPTS.contains(ext);
    }

    public static String resolveExtension(String relativePath) {
        final String fileName = Path.of(relativePath).getFileName().toString();
        final int index = fileName.lastIndexOf(".");
        return fileName.substring(index + 1);
    }

    public enum AutoDepsMode {
        ALL,
        STYLES,
        AUTO,
        NONE
    }

    public record AutoDeps(AutoDepsMode mode, Path nodeModulesDir, Predicate<String> idsPredicate) {
        public AutoDeps(AutoDepsMode mode, Path nodeModulesDir) {
            this(mode, nodeModulesDir, id -> true);
        }

    }
}
