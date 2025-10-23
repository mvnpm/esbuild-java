package io.mvnpm.esbuild.model;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public interface EsBuildPlugin {

    String name();

    String importScript();

    default void beforeBuild(Path workDir) {
    };

    String requireScript();

    default Object data() {
        return null;
    }

    default Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name());
        map.put("requireScript", requireScript());
        map.put("data", data());
        return map;
    }

}
