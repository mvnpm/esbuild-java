package io.mvnpm.esbuild.install;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import io.mvnpm.esbuild.Bundler;
import io.mvnpm.esbuild.model.WebDependency;

public record BuildDependency(String name, String fileName) {

    public static List<WebDependency> getDependencies() {
        return getWebDependencies(resolveBuildDependencies());
    }

    private static List<BuildDependency> resolveBuildDependencies() {
        Properties dependencies = resolveDependencies();
        List<BuildDependency> result = new ArrayList<>(dependencies.size());

        dependencies.forEach((key, value) -> {
            String name = key.toString().replaceAll("\\.version", "/%s".formatted(value));
            String fileName = name.replace("/", "-") + ".jar";
            result.add(new BuildDependency(name, fileName));
        });

        result.add(getEsbuildPlatformDependency(dependencies.getProperty("esbuild.version")));
        return result;
    }

    private static Properties resolveDependencies() {
        Properties properties = new Properties();
        try {
            final InputStream resource = Bundler.class.getResourceAsStream("/esbuild-java-version.properties");
            if (resource != null) {
                properties.load(resource);
            }
        } catch (IOException e) {
            // ignore we use the default
        }
        return properties;
    }

    private static List<WebDependency> getWebDependencies(List<BuildDependency> dependency) {
        return dependency.stream()
                .map(buildDependency -> Path.of(System.getProperty("user.home"), ".m2/repository/org/mvnpm/",
                        buildDependency.name(), buildDependency.fileName()))
                .map(d -> WebDependency.of("org.mvnpm:" + d.getFileName().toString().replace(".jar", ""), d,
                        WebDependency.WebDependencyType.MVNPM))
                .toList();
    }

    private static BuildDependency getEsbuildPlatformDependency(String version) {
        String classifier = determineClassifier();
        return new BuildDependency(String.format("at/esbuild/%s/%s", classifier, version),
                String.format("%s-%s.jar", classifier, version));
    }

    private static String determineClassifier() {
        final String osName = System.getProperty("os.name").toLowerCase();
        final String osArch = System.getProperty("os.arch").toLowerCase();
        String classifier;

        if (osName.contains("mac")) {
            if (osArch.equals("aarch64") || osArch.contains("arm")) {
                classifier = "darwin-arm64";
            } else {
                classifier = "darwin-x64";
            }
        } else if (osName.contains("win")) {
            classifier = osArch.contains("64") ? "win32-x64" : "win32-ia32";
        } else {
            if (osArch.equals("aarch64") || osArch.equals("arm64")) {
                classifier = "linux-arm64";
            } else if (osArch.contains("arm")) {
                classifier = "linux-arm";
            } else if (osArch.contains("64")) {
                classifier = "linux-x64";
            } else {
                classifier = "linux-ia32";
            }
        }
        return classifier;
    }
}
