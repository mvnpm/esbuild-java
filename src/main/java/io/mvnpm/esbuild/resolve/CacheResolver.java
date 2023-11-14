package io.mvnpm.esbuild.resolve;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CacheResolver extends BaseResolver implements Resolver {
    public CacheResolver(Resolver resolver) {
        super(resolver);
    }

    @Override
    public Path resolve(String version) throws IOException {
        final Path path = getLocation(version);
        final Path executable = path.resolve(EXECUTABLE_PATH);
        if (Files.isExecutable(executable)) {
            return executable;
        }
        return resolver.resolve(version);
    }
}
