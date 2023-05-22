package ch.nerdin.esbuild;

import ch.nerdin.esbuild.Bundler.BundleType;
import ch.nerdin.esbuild.util.Copy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Watch {

    private final Process process;
    public Watch(Process process) {
        this.process = process;
    }

    public void stop() {
        this.process.destroy();
    }

}
