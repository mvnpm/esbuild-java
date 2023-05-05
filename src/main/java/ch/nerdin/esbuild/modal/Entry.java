package ch.nerdin.esbuild.modal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Entry {

    List<Path> copyToLocation(Path location, List<Path> scripts) {
        return scripts.stream().map(script -> {
            try {
                final Path target = location.resolve(script.getFileName().toString());
                if (!target.toFile().exists())
                    Files.copy(script, target);
                return target;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    public abstract Path process(Path location);
}
