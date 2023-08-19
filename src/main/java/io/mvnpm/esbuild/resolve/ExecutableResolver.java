package io.mvnpm.esbuild.resolve;

import java.io.IOException;
import java.nio.file.Path;

public class ExecutableResolver implements Resolver {

    private final Resolver resolver;

    public ExecutableResolver() {
        final DownloadResolver downloadResolver = new DownloadResolver((version) -> {
            throw new RuntimeException("could not resolve esbuild with version " + version);
        });
        final BundledResolver bundledResolver = new BundledResolver(downloadResolver);
        this.resolver = new CacheResolver(bundledResolver);
    }

    @Override
    public Path resolve(String version) throws IOException {
        return resolver.resolve(version);
    }
}
