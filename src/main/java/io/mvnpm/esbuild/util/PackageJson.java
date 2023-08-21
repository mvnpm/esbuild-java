package io.mvnpm.esbuild.util;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
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
                logger.log(Level.FINEST, "package.json found in {0}", current);
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
        try (final InputStream packageStream = Files.newInputStream(packageJson)) {
            final JSONTokener jsonTokener = new JSONTokener(packageStream);
            final JSONObject object = new JSONObject(jsonTokener);
            return object.getString("name");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}