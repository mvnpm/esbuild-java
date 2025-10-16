package io.mvnpm.esbuild.install;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.mvnpm.esbuild.model.WebDependency;

public record EsBuildDeps(List<WebDependency> deps, List<String> plugins) {

    private static AtomicReference<EsBuildDeps> DEPS = new AtomicReference<>();
    private static final Pattern GAV_PATTERN = Pattern.compile(".*/(org/mvnpm(?:/[^/]+)*)/([^/]+)/([^/]+)/\\2-\\3\\.jar$");

    public static EsBuildDeps get() {
        return DEPS.updateAndGet(c -> {
            if (c == null) {
                c = fromClasspath();
            }
            return c;
        });
    }

    private static EsBuildDeps fromClasspath() {
        String classpath = System.getProperty("java.class.path");
        String[] entries = classpath.split(File.pathSeparator);
        final List<String> plugins = new ArrayList<>();
        final List<WebDependency> deps = Arrays.stream(entries)
                .map(Paths::get)
                .map(path -> {
                    final GAV gav = extractGav(path);
                    if (gav == null) {
                        return null;
                    }
                    final String gavString = gav.toString();
                    final Optional<WebDependency.WebDependencyType> type = WebDependency.WebDependencyType
                            .resolveType(gavString);
                    if (type.isEmpty()) {
                        return null;
                    }
                    if (gav.artifactId.matches("^esbuild-[a-z]+-plugin$")) {
                        plugins.add(gav.artifactId);
                    }
                    return WebDependency.of(gavString, path, type.get());
                })
                .filter(Objects::nonNull) // only keep dependencies with recognized type
                .collect(Collectors.toList());
        return new EsBuildDeps(deps, plugins);
    }

    private static GAV extractGav(Path jarPath) {
        String path = jarPath.toString().replace("\\", "/"); // normalize for Windows
        Matcher matcher = GAV_PATTERN.matcher(path);
        if (!matcher.matches()) {
            return null;
        }

        String groupId = matcher.group(1).replace("/", ".");
        String artifactId = matcher.group(2);
        String version = matcher.group(3);
        return new GAV(groupId, artifactId, version);
    }

    private record GAV(String groupId, String artifactId, String version) {
        @Override
        public String toString() {
            return groupId + ":" + artifactId + ":" + version;
        }
    }

}
