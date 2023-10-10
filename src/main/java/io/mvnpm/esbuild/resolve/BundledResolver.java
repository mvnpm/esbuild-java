package io.mvnpm.esbuild.resolve;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class BundledResolver extends BaseResolver implements Resolver {

    public BundledResolver(Resolver resolver) {
        super(resolver);
    }

    @Override
    public Path resolve(String version) throws IOException {
        final InputStream resource = getClass().getResourceAsStream("/%s-%s.tgz".formatted(CLASSIFIER, version));

        if (resource != null) {
            return extract(resource, version);
        }

        return resolver.resolve(version);
    }
}
