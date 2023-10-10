package io.mvnpm.esbuild;

import static io.mvnpm.esbuild.util.Copy.copyEntries;

import java.nio.file.Path;
import java.util.List;

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
