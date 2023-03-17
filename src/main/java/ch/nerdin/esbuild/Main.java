package ch.nerdin.esbuild;

import ch.nerdin.esbuild.resolve.ExecutableResolver;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {
        final Path esBuildExec = new ExecutableResolver().resolve(BundleDependencies.ESBUILD_VERSION);
        new Execute(esBuildExec.toFile(), args).execute();
    }
}
