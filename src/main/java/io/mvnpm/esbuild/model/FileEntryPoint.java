package io.mvnpm.esbuild.model;

import static io.mvnpm.esbuild.util.PathUtils.copyEntries;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record FileEntryPoint(Path rootDir, String script) implements EntryPoint {

    @Override
    public Path process(Path workDir) {
        if (!Objects.equals(rootDir, workDir)) {
            copyEntries(rootDir, List.of(script), workDir);
            return workDir.resolve(script);
        }
        final Path scriptPath = rootDir.resolve(script);
        if (!Files.exists(scriptPath)) {
            throw new UncheckedIOException(new IOException("Entry file not found: " + scriptPath));
        }
        return scriptPath;
    }
}
