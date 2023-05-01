package ch.nerdin.esbuild;

import ch.nerdin.esbuild.modal.EsBuildConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(Execute.class);

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
        watchOutput(command, () -> {

        });
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Process execute(BuildEventListener listener) throws IOException {
        final String[] command = args != null ? getCommand(args) : getCommand(esBuildConfig);
        watchOutput(command, listener);
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

    public void watchOutput(final String[] command, final BuildEventListener listener) throws IOException {
        process = new ProcessBuilder().command(command).start();
        final InputStream errorStream = process.getErrorStream();
        final Thread t = new Thread(new Streamer(errorStream, listener));
        t.setName("Process stdout streamer");
        t.setDaemon(true);
        t.start();
    }

    private record Streamer(InputStream processStream, BuildEventListener listener) implements Runnable {

        @Override
        public void run() {
            StringBuilder error = new StringBuilder();
            try (final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(processStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.debug(line);
                    if (line.contains("✘ [ERROR]") || !error.isEmpty()) {
                        error.append(line);
                    } else if (line.contains("build finished")) {
                        logger.info("Build finished!");
                        listener.onChange();
                    }
                }
            } catch (IOException e) {
                // ignore
            }
            if (!error.isEmpty()) {
                throw new BundleException(error.toString());
            }
        }
    }
}

