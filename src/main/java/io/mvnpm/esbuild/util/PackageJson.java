package io.mvnpm.esbuild.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PackageJson {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(PackageJson.class.getName());
    public static final String PACKAGE_JSON = "package.json";

    public static Optional<Path> findPackageJson(Path dir) {
        if (!Files.isDirectory(dir)) {
            return Optional.empty();
        }
        return findBreadthFirst(dir);
    }

    private static Optional<Path> findBreadthFirst(Path dir) {
        Queue<Path> queue = new ArrayDeque<>();
        queue.add(dir);
        while (!queue.isEmpty()) {
            Path current = queue.poll();
            if (Files.isRegularFile(current.resolve(PACKAGE_JSON))) {
                logger.log(Level.FINE, "package.json found in ''{0}''", current);
                return Optional.of(current.resolve(PACKAGE_JSON));
            }
            try (final Stream<Path> list = Files.list(current)) {
                list.sorted().filter(Files::isDirectory).filter(not(Files::isSymbolicLink)).forEach(queue::add);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.empty();
    }

    public static String readPackageName(Path packageJson) {
        try {
            JsonNode object = objectMapper.readTree(packageJson.toFile());
            return object.get("name").asText();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}