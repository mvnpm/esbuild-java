package io.mvnpm.esbuild.script;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

public interface DevProcess extends Closeable {
    void init();

    void build() throws IOException;

    Path workDir();

    Path dist();

    boolean isAlive();
}
