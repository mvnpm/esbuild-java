package ch.nerdin.esbuild.resolve;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class CacheResolver extends BaseResolver implements Resolver {
    public CacheResolver(Resolver resolver) {
        super(resolver);
    }

    @Override
    public Path resolve(String version) throws IOException {
        final File tmpdir = new File(System.getProperty("java.io.tmpdir"));
        File[] matchingFiles = tmpdir.listFiles((dir, name) -> name.startsWith("esbuild-" + version));

        if (matchingFiles != null && matchingFiles.length != 0) {
            final Path path = matchingFiles[0].toPath().resolve(EXECUTABLE_PATH);
            if (path.toFile().exists()) {
                return path;
            }
        }

        return resolver.resolve(version);
    }
}
