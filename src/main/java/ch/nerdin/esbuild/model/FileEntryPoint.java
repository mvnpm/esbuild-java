package ch.nerdin.esbuild.model;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static ch.nerdin.esbuild.util.Copy.copyEntries;

public class FileEntryPoint implements EntryPoint {

    private final Path rootDir;
    private final String script;

    public FileEntryPoint(Path rootDir, String script) {
        this.rootDir = rootDir;
        this.script = script;
    }

    @Override
    public Path process(Path workDir) {
        if (!Objects.equals(rootDir, workDir)) {
            copyEntries(rootDir, List.of(script), workDir);
            return workDir.resolve(script);
        }
        final Path scriptPath = rootDir.resolve(script);
        if(!Files.exists(scriptPath)) {
            throw new UncheckedIOException(new IOException("Entry file not found: " + scriptPath));
        }
        return scriptPath;
    }
}
