package io.mvnpm.esbuild;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import io.mvnpm.esbuild.model.EntryPoint;

public class Watch {

    private final Process process;
    private final Path workDir;

    private final Path dist;

    public Watch(Process process, Path workDir, Path dist) {
        this.process = process;
        this.workDir = workDir;
        this.dist = dist;
    }

    public void updateEntries(List<EntryPoint> entries) throws IOException {
        entries.forEach(entry -> entry.process(workDir));
    }

    public void stop() {
        process.destroy();
    }

    public Path workDir() {
        return workDir;
    }

    public Path dist() {
        return dist;
    }
}
