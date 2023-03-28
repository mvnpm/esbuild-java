package ch.nerdin.esbuild.resolve;

import org.apache.commons.text.StringSubstitutor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class DownloadResolver extends BaseResolver implements Resolver {
    private static final String URL_TEMPLATE = "https://registry.npmjs.org/@esbuild/${classifier}/-/${classifier}-${version}.tgz";
    private static final String FILE_NAME = "esbuild.tgz";

    public DownloadResolver(Resolver resolver) {
        super(resolver);
    }

    @Override
    public Path resolve(String version) throws IOException {
        final String url = new StringSubstitutor(Map.of(
                "classifier", determineClassifier(),
                "version", version
        )).replace(URL_TEMPLATE);

        final Path destination = createDestination(version);
        final Path tarFile = destination.resolve(FILE_NAME);

        try {
            downloadFile(new URL(url), tarFile);
            return extract(Files.newInputStream(tarFile), destination.toFile());
        } catch (IOException e) {
            return resolver.resolve(version);
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
