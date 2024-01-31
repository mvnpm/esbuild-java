package io.mvnpm.esbuild.resolve;

import static io.mvnpm.esbuild.Bundler.ESBUILD_EMBEDDED_VERSION;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BundleCacheResolver extends CacheResolver {
    public BundleCacheResolver(Resolver resolver) {
        super(resolver);
    }

    @Override
    public Path resolve(String version) throws IOException {
        final Path path = super.resolve(ESBUILD_EMBEDDED_VERSION);
        if (Files.isExecutable(path)) {
            return path;
        }
        return resolver.resolve(version);
    }
}
