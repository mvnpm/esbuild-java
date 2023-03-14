package io.quarkus.esbuild;

import java.io.IOException;
import java.nio.file.Path;

public interface BuildStep {

    Path execute() throws IOException;
}
