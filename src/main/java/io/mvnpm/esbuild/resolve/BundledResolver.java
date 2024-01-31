package io.mvnpm.esbuild.resolve;

import static io.mvnpm.esbuild.Bundler.ESBUILD_EMBEDDED_VERSION;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class BundledResolver extends BaseResolver implements Resolver {

    public BundledResolver(Resolver resolver) {
        super(resolver);
    }

    @Override
    public Path resolve(String version) throws IOException {
        final InputStream resource = getClass().getResourceAsStream("/esbuild-%s-%s.tgz".formatted(CLASSIFIER, version));

        if (resource != null) {
            return extract(resource, ESBUILD_EMBEDDED_VERSION);
        }

        return resolver.resolve(version);
    }
}
