package io.mvnpm.esbuild.model;

import java.io.Closeable;

public record WatchStartResult(WatchBuildResult firstBuildResult, WatchProcess process) {

    public interface WatchProcess extends Closeable {

        boolean isAlive();

    }

}
