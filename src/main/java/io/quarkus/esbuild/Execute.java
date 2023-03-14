package io.quarkus.esbuild;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Execute implements BuildStep {

    private final File esBuildExec;
    private Config config;
    private String[] args;

    public Execute(File esBuildExec, Config config) {
        this.esBuildExec = esBuildExec;
        this.config = config;
    }

    public Execute(File esBuildExec, String[] args) {
        this.esBuildExec = esBuildExec;
        this.args = args;
    }

    @Override
    public Path execute() throws IOException {
        ProcessBuilder builder = new ProcessBuilder();

        final String[] command = args != null ? getCommand(args) : getCommand(config);
        builder.command(command);
        builder.inheritIO();
        Process process = builder.start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private String[] getCommand(Config config) {
        String[] params = config.toParams();
        return getCommand(params);
    }

    private String[] getCommand(String[] args) {
        List<String> argList = new ArrayList<>(args.length + 1);
        argList.add(esBuildExec.toString());
        argList.addAll(Arrays.asList(args));

        return argList.toArray(new String[0]);
    }
}

