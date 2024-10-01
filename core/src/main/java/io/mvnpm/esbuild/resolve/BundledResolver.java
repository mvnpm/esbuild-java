package io.mvnpm.esbuild.resolve;

import static io.mvnpm.esbuild.resolve.Resolvers.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class BundledResolver implements Resolver {
    private static final Logger logger = Logger.getLogger(DownloadResolver.class.getName());
    private final Resolver fallbackResolver;

    public BundledResolver(Resolver fallbackResolver) {
        this.fallbackResolver = fallbackResolver;
    }

    @Override
    public Path resolve(String version) throws IOException {
        final Path path = getLocation(version);
        final Path executablePath = resolveExecutablePath(path);
        if (Files.isExecutable(path)) {
            return executablePath;
        }

        final String tgz = getTgzPath(version);
        final InputStream resource = getClass().getResourceAsStream(tgz);

        if (resource != null) {
            final Path bundleDir = extract(resource, version);
            return requireExecutablePath(bundleDir);
        }

        return fallbackResolver.resolve(version);

    }

}
