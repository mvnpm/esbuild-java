package io.quarkus.esbuild;

import org.apache.commons.text.StringSubstitutor;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class Download implements BuildStep {

    private static final String URL_TEMPLATE = "https://registry.npmjs.org/@esbuild/${classifier}/-/${classifier}-${version}.tgz";
    private static final String FILE_NAME = "esbuild.tgz";
    private final String version;


    Download(String version) {
        this.version = version;
    }

    @Override
    public Path execute() throws IOException {
        final String url = determineBuildUrl();
        Path tempDirectory = Files.createTempDirectory("esbuild");
        final Path destination = tempDirectory.resolve(FILE_NAME);
        downloadFile(new URL(url), destination);
        extract(destination.toFile(), tempDirectory.toFile());
        return tempDirectory.resolve("package/bin/esbuild");
    }

    private String determineBuildUrl() {
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

        return new StringSubstitutor(Map.of("classifier", classifier,
                "version", version
        )).replace(URL_TEMPLATE);
    }

    void downloadFile(URL url, Path destination) throws IOException {
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        try (FileOutputStream fileOutputStream = new FileOutputStream(destination.toFile())) {
            fileOutputStream.getChannel()
                    .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }
    }

    void extract(File archive, File destination) throws IOException {
        Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
        archiver.extract(archive, destination);
    }

}
