package ch.nerdin.esbuild;

import ch.nerdin.esbuild.Bundler.BundleType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Watch {

    private final Process process;
    private final Path workingFolder;
    private final BundleType type;

    public Watch(Process process, Path workingFolder, BundleType type) {
        this.process = process;
        this.workingFolder = workingFolder;
        this.type = type;
    }

    public void stop() {
        this.process.destroy();
    }

    public void change(List<Path> dependencies, List<Path> entries) throws IOException {
        Bundler.extract(workingFolder, dependencies, type);
        change(entries);
    }

    public void change(List<Path> entries) {
        entries.stream().map(script -> {
            try {
                return Files.copy(script, workingFolder.resolve(script.getFileName()), REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
