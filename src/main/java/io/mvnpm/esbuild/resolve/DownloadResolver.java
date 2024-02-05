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
    private static final String FILE_NAME = "esbuild.tgz";

    @Override
    public Path resolve(String version) throws IOException {
        final Path path = getLocation(version);
        final String resolveExecutableRelativePath = resolveExecutablePath();
        final Path executable = path.resolve(resolveExecutableRelativePath);
        if (Files.isExecutable(executable)) {
            return executable;
        }

        final String url = URL_TEMPLATE.formatted(CLASSIFIER, version);

        final Path destination = createDestination(version);
        final Path tarFile = destination.resolve(FILE_NAME);

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
}
