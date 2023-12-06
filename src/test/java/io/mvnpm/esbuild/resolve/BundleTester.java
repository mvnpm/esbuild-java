package io.mvnpm.esbuild.resolve;

import java.io.IOException;

import io.mvnpm.esbuild.Bundler;
import io.mvnpm.esbuild.util.PathUtils;

public abstract class BundleTester {

    public static void cleanUp(String version) throws IOException {
        PathUtils.deleteRecursive(BaseResolver.getLocation(version));
    }

    public static void cleanUpDefault() throws IOException {
        cleanUp(Bundler.ESBUILD_EMBEDDED_VERSION);
    }
}
