package io.mvnpm.esbuild.model;

public record WatchStartResult(WatchBuildResult firstBuildResult, Process process) {
}
