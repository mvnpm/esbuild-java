package io.mvnpm.esbuild.model;

import io.mvnpm.esbuild.BundleException;

public record WatchBuildResult(String output, BundleException bundleException) {
    public WatchBuildResult(String output) {
        this(output, null);
    }

    public boolean isSuccess() {
        return bundleException == null;
    }
}
