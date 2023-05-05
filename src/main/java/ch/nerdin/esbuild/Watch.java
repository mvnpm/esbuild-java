package ch.nerdin.esbuild;

import ch.nerdin.esbuild.Bundler.BundleType;
import ch.nerdin.esbuild.util.Copy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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
        entries.forEach(script -> Copy.copy(script, workingFolder.resolve(script.getFileName())));
    }
}
