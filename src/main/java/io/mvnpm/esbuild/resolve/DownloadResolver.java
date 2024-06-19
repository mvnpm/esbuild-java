package io.mvnpm.esbuild.resolve;

import static io.mvnpm.esbuild.resolve.Resolvers.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class DownloadResolver implements Resolver {
    private static final String URL_TEMPLATE = "https://registry.npmjs.org/@esbuild/%1$s/-/%1$s-%2$s.tgz";
    public static final String SCSS_TEMPLATE = "https://github.com/mvnpm/esbuild/releases/download/v%1$s/esbuild-%2$s-%3$s.tgz";
    private static final String FILE_NAME = "esbuild.tgz";

    @Override
    public Path resolve(String version) throws IOException {
        final String url = getDownloadUrl(version);
        final Path destination = createDestination(version);
        final Path tarFile = destination.resolve(FILE_NAME);

        final String resolveExecutableRelativePath = resolveExecutablePath();

        try {
            downloadFile(new URL(url), tarFile);
            return extract(Files.newInputStream(tarFile), destination.toFile()).resolve(resolveExecutableRelativePath);
        } catch (IOException e) {
            throw new UncheckedIOException("could not resolve esbuild with version " + version, e);
        }
    }

    void downloadFile(URL url, Path destination) throws IOException {
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        try (FileOutputStream fileOutputStream = new FileOutputStream(destination.toFile())) {
            fileOutputStream.getChannel()
                    .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }
    }

    private String getDownloadUrl(String version) {
        if (version.contains("mvnpm")) {
            return SCSS_TEMPLATE.formatted(version.substring(version.lastIndexOf("-") + 1), CLASSIFIER, version);
        }

        return URL_TEMPLATE.formatted(CLASSIFIER, version);
    }
}
