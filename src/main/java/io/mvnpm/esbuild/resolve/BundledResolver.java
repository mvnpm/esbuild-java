package io.mvnpm.esbuild.resolve;

import static io.mvnpm.esbuild.resolve.Resolvers.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class BundledResolver implements Resolver {

    private final Resolver fallbackResolver;

    public BundledResolver(Resolver fallbackResolver) {
        this.fallbackResolver = fallbackResolver;
    }

    @Override
    public Path resolve(String version) throws IOException {
        final Path path = getLocation(version);
        final String bundledExecutableRelativePath = resolveBundledExecutablePath();
        final Path executablePath = path.resolve(bundledExecutableRelativePath);
        if (Files.isExecutable(executablePath)) {
            return executablePath;
        }

        final InputStream resource = getClass().getResourceAsStream("/esbuild-%s-%s.tgz".formatted(CLASSIFIER, version));

        if (resource != null) {
            return extract(resource, version).resolve(bundledExecutableRelativePath);
        }

        return fallbackResolver.resolve(version);
    }
}
