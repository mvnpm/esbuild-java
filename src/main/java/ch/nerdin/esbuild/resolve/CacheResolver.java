package ch.nerdin.esbuild.resolve;

import java.io.IOException;
import java.nio.file.Path;

public class CacheResolver extends BaseResolver implements Resolver {
    public CacheResolver(Resolver resolver) {
        super(resolver);
    }

    @Override
    public Path resolve(String version) throws IOException {
        final Path path = getLocation(version);
        if (path.toFile().exists()) {
            return path.resolve(EXECUTABLE_PATH);
        }

        return resolver.resolve(version);
    }
}
