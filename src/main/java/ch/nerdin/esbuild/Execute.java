package ch.nerdin.esbuild;

import ch.nerdin.esbuild.modal.EsBuildConfig;
import ch.vorburger.exec.ManagedProcess;
import ch.vorburger.exec.ManagedProcessBuilder;
import ch.vorburger.exec.ManagedProcessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Execute {
    private static final Logger logger = LoggerFactory.getLogger(Execute.class);

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

    public void executeAndWait() {
        watchOutput(getCommand(), null);
    }

    protected ManagedProcess execute(BuildEventListener listener) {
        return watchOutput(getCommand(), listener);
    }

    private String[] getCommand() {
        String[] command = args != null ? getCommand(args) : getCommand(esBuildConfig);
        if (logger.isDebugEnabled()) {
            logger.debug("running esbuild with flags: `{}`.", String.join(" ", command));
        }
        return command;
    }

    private String[] getCommand(EsBuildConfig esBuildConfig) {
        String[] params = esBuildConfig.toParams();
        return getCommand(params);
    }

    private String[] getCommand(String[] args) {
        List<String> argList = new ArrayList<>(args.length + 1);
        argList.addAll(Arrays.asList(args));

        return argList.toArray(new String[0]);
    }

    public ManagedProcess watchOutput(final String[] command, final BuildEventListener listener) {
        try {
            ManagedProcessBuilder pb = new ManagedProcessBuilder(esBuildExec.toString());
            Arrays.stream(command).forEach(pb::addArgument);

            if (listener != null) {
                pb.addStdErr(new OutputStream() {
                    private StringBuilder buffer = new StringBuilder();

                    @Override
                    public void write(int b) {
                        if (buffer.toString().toLowerCase().contains("build finished")) {
                            listener.onChange();
                        }
                        if (b == '\n') {
                            buffer = new StringBuilder();
                        }
                        buffer.append((char) b);
                    }
                });
            }

            final ManagedProcess managedProcess = pb.build();
            managedProcess.start();
            if (listener == null) {
                managedProcess.waitForExit();
            }

            return managedProcess;
        } catch (ManagedProcessException e) {
            throw new BundleException(e);
        }
    }
}

