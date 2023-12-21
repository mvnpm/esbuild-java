package io.mvnpm.esbuild;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.mvnpm.esbuild.model.ExecuteResult;
import io.mvnpm.esbuild.resolve.ExecutableResolver;

public class Main {

    public static void main(String[] args) throws IOException {
        String workingDirectory = System.getProperty("user.dir");
        final Path esBuildExec = new ExecutableResolver().resolve(Bundler.ESBUILD_EMBEDDED_VERSION);
        final ExecuteResult executeResult = new Execute(Paths.get(workingDirectory), esBuildExec.toFile(), args)
                .executeAndWait();
        System.out.println(executeResult.output());
    }
}
