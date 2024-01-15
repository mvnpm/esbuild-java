package io.mvnpm.esbuild;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.mvnpm.esbuild.model.ExecuteResult;
import io.mvnpm.esbuild.resolve.ExecutableResolver;

public class Main {

    public static void main(String[] args) throws IOException {
        String workingDirectory = System.getProperty("user.dir");
        final List<String> arguments = new ArrayList<>(Arrays.asList(args));
        final Optional<String> esbuildVersionArgument = arguments.stream().filter(a -> a.startsWith("--esbuildVersion"))
                .findFirst();
        String esbuildVersion = Bundler.ESBUILD_EMBEDDED_VERSION;
        if (esbuildVersionArgument.isPresent()) {
            arguments.remove(esbuildVersionArgument.get());
            final String unParsedEsbuildVersion = esbuildVersionArgument.get();
            esbuildVersion = unParsedEsbuildVersion.split("=")[1];
        }
        final Path esBuildExec = new ExecutableResolver().resolve(esbuildVersion);
        final ExecuteResult executeResult = new Execute(Paths.get(workingDirectory), esBuildExec.toFile(),
                arguments.toArray(new String[] {}))
                .executeAndWait();
        System.out.println(executeResult.output());
    }
}
