package io.mvnpm.esbuild;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import io.mvnpm.esbuild.install.EsBuildDeps;
import io.mvnpm.esbuild.model.BundleOptions;
import io.mvnpm.esbuild.model.BundleResult;
import io.mvnpm.esbuild.model.EsBuildConfig;

public class Main {

    public static void main(String[] args) throws IOException {
        final String entrypoint = args.length > 0 ? args[0] : "example.js";
        System.out.println("Using entrypoint: " + entrypoint);
        final BundleOptions options = BundleOptions.builder()
                .addEntryPoint(Path.of(entrypoint))
                .build();
        final BundleResult result = Bundler.bundle(options, true);
        System.out.println(result.logs());
        System.out.println("Bundling output: " + result.dist());
    }
}
