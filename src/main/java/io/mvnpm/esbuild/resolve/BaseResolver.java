package io.mvnpm.esbuild.resolve;

import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

public abstract class BaseResolver {
    public static final String EXECUTABLE_PATH = "package/bin/esbuild";

    protected Resolver resolver;

    public BaseResolver(Resolver resolver) {
        this.resolver = requireNonNull(resolver, "resolver is required");
    }

    static String determineClassifier() {
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

    static Path extract(InputStream archive, String version) throws IOException {
        final File destination = createDestination(version).toFile();
        return extract(archive, destination);
    }

    static Path extract(InputStream archive, File destination) throws IOException {
        Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
        archiver.extract(archive, destination);
        return destination.toPath().resolve(EXECUTABLE_PATH);
    }

    static Path createDestination(String version) throws IOException {
        return Files.createDirectories(getLocation(version));
    }

    static Path getLocation(String version) {
        return Path.of(System.getProperty("java.io.tmpdir")).resolve("esbuild-" + version);
    }
}
