package ch.nerdin.esbuild.resolve;

import ch.nerdin.esbuild.Bundler;
import ch.nerdin.esbuild.util.Copy;

import java.io.IOException;

public abstract class BundleTester {


    public static void cleanUp(String version) throws IOException {
        Copy.deleteRecursive(BaseResolver.getLocation(version));
    }

    public static void cleanUpDefault() throws IOException {
        cleanUp(Bundler.getDefaultVersion());
    }
}
