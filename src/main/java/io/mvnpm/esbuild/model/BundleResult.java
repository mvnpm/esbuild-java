package io.mvnpm.esbuild.model;

import java.nio.file.Path;

public record BundleResult(Path dist, Path workDir, ExecuteResult result) {
}
