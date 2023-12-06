package io.mvnpm.esbuild.model;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;

public record WebDependency(String id, Path path, WebDependencyType type) {

    public static WebDependency of(String id, Path path, WebDependencyType type) {
        return new WebDependency(id, path, type);
    }

    public static WebDependency of(Path path, WebDependencyType type) {
        final String fileName = path.getFileName().toString();
        return new WebDependency(fileName.substring(0, fileName.lastIndexOf(".")), path, type);
    }

    public enum WebDependencyType {
        WEBJARS(s -> s.startsWith("org.webjars.npm")),
        MVNPM(s -> s.startsWith("org.mvnpm"));

        private final Predicate<String> gavMatcher;

        WebDependencyType(Predicate<String> gavMatcher) {
            this.gavMatcher = gavMatcher;
        }

        public boolean matches(String gav) {
            return this.gavMatcher.test(gav);
        }

        public static boolean anyMatch(String gav) {
            return resolveType(gav).isPresent();
        }

        public static Optional<WebDependencyType> resolveType(String gav) {
            for (WebDependencyType value : values()) {
                if (value.matches(gav)) {
                    return Optional.of(value);
                }
            }
            return Optional.empty();
        }
    }
}
