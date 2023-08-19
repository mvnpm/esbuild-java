package io.mvnpm.esbuild.resolve;

import java.io.IOException;
import java.nio.file.Path;

public interface Resolver {
    Path resolve(String version) throws IOException;
}
