package io.mvnpm.esbuild.model;

import io.mvnpm.esbuild.BundlingException;

public record WatchBuildResult(String output, BundlingException bundlingException) {
    public WatchBuildResult(String output) {
        this(output, null);
    }

    public boolean isSuccess() {
        return bundlingException == null;
    }
}
