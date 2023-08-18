package ch.nerdin.esbuild;

import ch.nerdin.esbuild.Bundler.BundleType;
import ch.nerdin.esbuild.util.Copy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static ch.nerdin.esbuild.util.Copy.copyEntries;

public class Watch {

    private final Process process;
    private final Path workingFolder;

    public Watch(Process process, Path workingFolder) {
        this.process = process;
        this.workingFolder = workingFolder;
    }

    public void change(Path sourceDir, List<String> entries) {
        copyEntries(sourceDir, entries, workingFolder);
    }

    public void stop() {
        process.destroy();
    }

}