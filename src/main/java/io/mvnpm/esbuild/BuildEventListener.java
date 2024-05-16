package io.mvnpm.esbuild;

import io.mvnpm.esbuild.model.WatchBuildResult;

public interface BuildEventListener {

    void onBuild(WatchBuildResult result);

}
