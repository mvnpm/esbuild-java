package ch.nerdin.esbuild;

import ch.nerdin.esbuild.util.Copy;
import ch.vorburger.exec.ManagedProcess;
import ch.vorburger.exec.ManagedProcessException;

import java.nio.file.Path;
import java.util.List;

public class Watch {

    private final ManagedProcess process;
    private final Path workingFolder;

    public Watch(ManagedProcess process, Path workingFolder) {
        this.process = process;
        this.workingFolder = workingFolder;
    }

    public void change(List<Path> entries) {
        entries.forEach(script -> Copy.copy(script, workingFolder.resolve(script.getFileName())));
    }

    public void stop() {
        try {
            process.destroy();
        } catch (ManagedProcessException e) {
            throw new RuntimeException(e);
        }
    }

}
