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
        final Path path = getLocation(ESBUILD_EMBEDDED_VERSION);
        final Path executablePath = path.resolve(resolveBundledExecutablePath());
        if (Files.isExecutable(executablePath)) {
            return executablePath;
        }
        return resolver.resolve(version);
    }
}
