package io.mvnpm.esbuild.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record EsBuildConfig(
        String esBuildVersion,
        boolean bundle,
        String[] entryPoint,
        boolean minify,

        boolean version,

        Map<String, Loader> loader,
        boolean preserveSymlinks,
        Target target,

        boolean watch,

        String outdir,
        String packages,

        Platform platform,

        boolean serve,
        boolean sourcemap,

        boolean splitting,

        Map<String, String> alias,

        Map<String, String> define,

        List<String> excludes,
        Format format,

        String chunkNames,

        String entryNames,

        String assetNames,

        String publicPath,

        List<String> external) {

    public static EsBuildConfigBuilder builder() {
        return new EsBuildConfigBuilder().withDefault();
    }

    public EsBuildConfig(EsBuildConfigBuilder builder) {
        this(builder.esBuildVersion,
                builder.bundle,
                builder.entryPoint,
                builder.minify,

                builder.version,

                builder.loader,
                builder.preserveSymlinks,
                builder.target,

                builder.watch,

                builder.outdir,
                builder.packages,

                builder.platform,

                builder.serve,
                builder.sourceMap,

                builder.splitting,

                builder.alias,
                builder.define,

                builder.excludes,
                builder.format,

                builder.chunkNames,

                builder.entryNames,

                builder.assetNames,

                builder.publicPath,

                builder.external);
    }

    public EsBuildConfigBuilder edit() {
        return new EsBuildConfigBuilder()
                .esbuildVersion(this.esBuildVersion)
                .bundle(this.bundle)
                .entryPoint(this.entryPoint)
                .minify(this.minify)
                .version(this.version)
                .loader(this.loader)
                .preserveSymlinks(this.preserveSymlinks)
                .target(this.target)
                .watch(this.watch)
                .outDir(this.outdir)
                .packages(this.packages)
                .platform(this.platform)
                .serve(this.serve)
                .sourceMap(this.sourcemap)
                .splitting(this.splitting)
                .alias(this.alias)
                .define(this.define)
                .excludes(this.excludes)
                .format(this.format)
                .chunkNames(this.chunkNames)
                .entryNames(this.entryNames)
                .assetNames(this.assetNames)
                .publicPath(this.publicPath)
                .external(this.external);
    }

    public enum Format {
        IIFE,
        CJS,
        ESM,
    }

    public enum Loader {
        BASE64,
        BINARY,
        COPY,
        CSS,
        DATAURL,
        LOCAL_CSS,
        GLOBAL_CSS,
        EMPTY,
        FILE,
        JS,
        JSON,
        JSX,
        TEXT,
        TS,
        TSX;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }

    }

    public enum Platform {
        BROWSER,
        NODE,
        NEUTRAL
    }

    public enum Target {
        ES2017,
        CHROME58,
        FIREFOX57,
        SAFARI11,
        EDGE16,
        NODE10,
        IE9,
        OPERA45
    }

    public String[] toParams() {
        final Field[] fields = EsBuildConfig.class.getDeclaredFields();
        List<String> result = new ArrayList<>(fields.length);
        for (Field field : fields) {
            try {
                final Object value = field.get(this);
                if (value != null) {
                    final String fieldName = field.getName();
                    if (value == Boolean.TRUE) {
                        result.add("--" + convertField(fieldName));
                    } else if (value instanceof List) {
                        ((List<?>) value).forEach(e -> result.add("--%s:%s".formatted(convertField(fieldName), e.toString())));
                    } else if (value instanceof Map) {
                        result.addAll(mapToString(fieldName, (Map<?, ?>) value));
                    } else if ("entryPoint".equals(field.getName())) {
                        result.addAll(List.of((String[]) value));
                    } else if (!(value instanceof Boolean)) {
                        String fn = convertField(fieldName);
                        String v = value.toString();
                        if (!fn.equals("outdir")) {
                            v = v.toLowerCase();
                        }
                        result.add("--%s=%s".formatted(fn, v));
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return result.toArray(String[]::new);
    }

    private static String convertField(String field) {
        final String[] split = field.split("(?=\\p{Upper})");
        return Arrays.stream(split).map(String::toLowerCase).collect(Collectors.joining("-"));
    }

    private static List<String> mapToString(String fieldName, Map<?, ?> map) {
        List<String> result = new ArrayList<>(map.size());
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            result.add("--%s:%s=%s".formatted(fieldName, entry.getKey(),
                    entry.getValue().toString().replaceAll("_", "-")));
        }

        return result;
    }
}
