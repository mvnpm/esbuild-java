package io.mvnpm.esbuild.model;

import java.nio.file.Path;

public record WebDependency(String id, Path path, WebDependencyType type) {

    public static WebDependency of(String id, Path path, WebDependencyType type) {
        return new WebDependency(id, path, type);
    }

    public static WebDependency of(Path path, WebDependencyType type) {
        final String fileName = path.getFileName().toString();
        return new WebDependency(fileName.substring(0, fileName.lastIndexOf(".")), path, type);
    }

    public enum WebDependencyType {
        WEBJARS,
        MVNPM
    }
}
