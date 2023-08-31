package io.mvnpm.esbuild.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JarInspector {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(JarInspector.class.getName());
    public static final String PACKAGE_JSON = "package.json";
    public static final String POM_PROPERTIES = "pom.properties";
    
    public static List<Path> findPackageJsons(Path dir, boolean multiple) {
        return findAny(dir, multiple, PACKAGE_JSON);
    }

    public static Optional<Path> findPomProperties(Path dir){
        return findAnyBreadthFirst(dir, POM_PROPERTIES);
    }
    
    private static Optional<Path> findAnyBreadthFirst(Path root, String fileName) {
        List<Path> found = findAny(root, false, fileName);
        if(found.isEmpty()){
            return Optional.empty();
        }
        return Optional.of(found.get(0));
    }
    
    private static List<Path> findAny(Path root,boolean multiple, String fileName) {
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        List<Path> paths = new ArrayList<>();
        Queue<Path> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Path current = queue.poll();
            if (Files.isRegularFile(current.resolve(fileName))) {
                logger.log(Level.FINE, fileName + " found in ''{0}''", current);
                Path found = current.resolve(fileName);
                if(multiple){
                    paths.add(found);
                }else{
                    return List.of(found);
                }
            }
            try (final Stream<Path> list = Files.list(current)) {
                list.sorted().filter(Files::isDirectory).filter(not(Files::isSymbolicLink)).forEach(queue::add);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return paths;
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