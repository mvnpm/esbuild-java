package ch.nerdin.esbuild;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Execute {

    private final File esBuildExec;
    private EsBuildConfig esBuildConfig;
    private String[] args;

    public Execute(File esBuildExec, EsBuildConfig esBuildConfig) {
        this.esBuildExec = esBuildExec;
        this.esBuildConfig = esBuildConfig;
    }

    public Execute(File esBuildExec, String[] args) {
        this.esBuildExec = esBuildExec;
        this.args = args;
    }

    public void execute() throws IOException {
        ProcessBuilder builder = new ProcessBuilder();

        final String[] command = args != null ? getCommand(args) : getCommand(esBuildConfig);
        builder.command(command);
        builder.inheritIO();
        Process process = builder.start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getCommand(EsBuildConfig esBuildConfig) {
        String[] params = esBuildConfig.toParams();
        return getCommand(params);
    }

    private String[] getCommand(String[] args) {
        List<String> argList = new ArrayList<>(args.length + 1);
        argList.add(esBuildExec.toString());
        argList.addAll(Arrays.asList(args));

        return argList.toArray(new String[0]);
    }
}

