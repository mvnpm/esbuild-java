package ch.nerdin.esbuild.modal;

import java.nio.file.Path;
import java.util.List;

public class FileEntry extends Entry {
    private Path script;

    public FileEntry(Path script) {
        this.script = script;
    }

    @Override
    public Path getEntry(Path location) {
        copyToLocation(location, List.of(script));
        return location.resolve(script.getFileName());
    }
}
