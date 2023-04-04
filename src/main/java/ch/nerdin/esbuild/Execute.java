package ch.nerdin.esbuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Execute {

    private final File esBuildExec;
    private EsBuildConfig esBuildConfig;
    private String[] args;
    private Process process;

    public Execute(File esBuildExec, EsBuildConfig esBuildConfig) {
        this.esBuildExec = esBuildExec;
        this.esBuildConfig = esBuildConfig;
    }

    public Execute(File esBuildExec, String[] args) {
        this.esBuildExec = esBuildExec;
        this.args = args;
    }

    public void executeAndWait() throws IOException {
        final String[] command = args != null ? getCommand(args) : getCommand(esBuildConfig);
        final Process process =  new ProcessBuilder().command(command).inheritIO().start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Process execute(BuildEventListener listener) throws IOException {
        final String[] command = args != null ? getCommand(args) : getCommand(esBuildConfig);
        process = new ProcessBuilder().command(command).start();
        watchOutput(process, listener);
        return process;
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

    public static void watchOutput(final Process process, final BuildEventListener listener) {
        final InputStream errorStream = process.getErrorStream();
        final Thread t = new Thread(new Streamer(errorStream, listener));
        t.setName("Process stdout streamer");
        t.setDaemon(true);
        t.start();
    }

    private static final class Streamer implements Runnable {

        private final InputStream processStream;
        private final BuildEventListener listener;

        private Streamer(final InputStream processStream, final BuildEventListener listener) {
            this.processStream = processStream;
            this.listener = listener;
        }

        @Override
        public void run() {
            try (final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(processStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    if (line.contains("build finished")) {
                        listener.onChange();
                    }
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }
}

