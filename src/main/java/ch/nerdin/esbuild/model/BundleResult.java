package ch.nerdin.esbuild.model;

import java.nio.file.Path;

public record BundleResult(Path dist, ExecuteResult result) {
}
