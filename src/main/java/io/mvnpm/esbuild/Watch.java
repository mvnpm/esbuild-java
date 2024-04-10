package io.mvnpm.esbuild;

import static io.mvnpm.esbuild.Bundler.getNodeModulesDir;

import java.io.IOException;
import java.nio.file.Path;

import io.mvnpm.esbuild.install.WebDepsInstaller;
import io.mvnpm.esbuild.model.BundleOptions;

public class Watch {

    private final Process process;
    private final Path workingFolder;

    public Watch(Process process, Path workingFolder) {
        this.process = process;
        this.workingFolder = workingFolder;
    }

    public void change(BundleOptions bundleOptions) throws IOException {
        Path nodeModulesDir = getNodeModulesDir(workingFolder, bundleOptions);
        WebDepsInstaller.install(nodeModulesDir, bundleOptions.getDependencies());
        bundleOptions.getEntries().forEach(entry -> entry.process(workingFolder));
    }

    public void stop() {
        process.destroy();
    }

}
