package io.mvnpm.esbuild.model;

import java.nio.file.Path;

public interface EntryPoint {

    Path process(Path workDir);
}
