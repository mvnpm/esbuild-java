package io.quarkus.esbuild;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {
        final Path esBuildExec = new Download("0.17.10").execute();
        new Execute(esBuildExec.toFile(), args).execute();
    }
}
