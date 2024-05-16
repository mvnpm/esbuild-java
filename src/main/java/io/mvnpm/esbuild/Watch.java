package io.mvnpm.esbuild;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import io.mvnpm.esbuild.model.EntryPoint;
import io.mvnpm.esbuild.model.WatchBuildResult;

public class Watch {

    private final Process process;
    private final Path workDir;

    private final Path dist;

    private final WatchBuildResult firstBuildResult;

    public Watch(Process process, Path workDir, Path dist, WatchBuildResult firstBuildResult) {
        this.process = process;
        this.workDir = workDir;
        this.dist = dist;
        this.firstBuildResult = firstBuildResult;
    }

    public void updateEntries(List<EntryPoint> entries) throws IOException {
        entries.forEach(entry -> entry.process(workDir));
    }

    public void stop() {
        process.destroy();

    }

    public void waitForStop() {
        process.destroy();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public Path workDir() {
        return workDir;
    }

    public WatchBuildResult firstBuildResult() {
        return firstBuildResult;
    }

    public boolean isAlive() {
        return process.isAlive();
    }

    public Path dist() {
        return dist;
    }
}
