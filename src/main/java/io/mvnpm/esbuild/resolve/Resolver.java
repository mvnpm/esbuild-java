package io.mvnpm.esbuild.resolve;

import java.io.IOException;
import java.nio.file.Path;

public interface Resolver {
    Path resolve(String version) throws IOException;

    static Resolver create() {
        final DownloadResolver downloadResolver = new DownloadResolver();
        return new BundledResolver(downloadResolver);
    }
}
