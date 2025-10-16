package io.mvnpm.esbuild.model;

import java.io.Closeable;
import java.io.IOException;

import io.mvnpm.esbuild.script.DevProcess;

public record DevResult(DevProcess process) implements Closeable {

    @Override
    public void close() throws IOException {
        process.close();
    }
}
