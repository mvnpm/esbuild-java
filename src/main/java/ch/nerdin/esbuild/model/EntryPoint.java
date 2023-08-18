package ch.nerdin.esbuild.model;

import java.nio.file.Path;

public interface EntryPoint {

    Path process(Path workDir);
}
