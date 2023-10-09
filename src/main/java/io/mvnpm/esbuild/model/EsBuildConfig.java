package io.mvnpm.esbuild.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EsBuildConfig {
    private boolean bundle;

    private String[] entryPoint;
    private boolean minify;

    private boolean version;

    private Map<String, String> substitutes;

    private List<String> excludes;

    enum Format {
        IIFE,
        CJS,
        ESM,
    }

    private Format format;

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
        TSX
    }

    private Map<String, Loader> loader;

    private String outdir;
    private String packages;

    enum Platform {
        BROWSER,
        NODE,
        NEUTRAL
    }

    private Platform platform;

    private boolean serve;
    private boolean sourceMap;

    private boolean splitting;

    enum Target {
        ES2017,
        CHROME58,
        FIREFOX57,
        SAFARI11,
        EDGE16,
        NODE10,
        IE9,
        OPERA45
    }

    private Target target;

    private String watch;

    private String chunkNames;

    private String entryNames;

    private String assetNames;

    private final List<String> external = new ArrayList<>();

    public boolean isBundle() {
        return bundle;
    }

    public void setBundle(boolean bundle) {
        this.bundle = bundle;
    }

    public String[] getEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(String[] entryPoint) {
        this.entryPoint = entryPoint;
    }

    public boolean isMinify() {
        return minify;
    }

    public void setMinify(boolean minify) {
        this.minify = minify;
    }

    public boolean isVersion() {
        return version;
    }

    public void setVersion(boolean version) {
        this.version = version;
    }

    public Map<String, String> getSubstitutes() {
        return substitutes;
    }

    public void setSubstitutes(Map<String, String> substitutes) {
        this.substitutes = substitutes;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public Map<String, Loader> getLoader() {
        return loader;
    }

    public void setLoader(Map<String, Loader> loader) {
        this.loader = loader;
    }

    public String getOutdir() {
        return outdir;
    }

    public void setOutdir(String outdir) {
        this.outdir = outdir;
    }

    public String getPackages() {
        return packages;
    }

    public void setPackages(String packages) {
        this.packages = packages;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public boolean isServe() {
        return serve;
    }

    public void setServe(boolean serve) {
        this.serve = serve;
    }

    public boolean isSourceMap() {
        return sourceMap;
    }

    public void setSourceMap(boolean sourceMap) {
        this.sourceMap = sourceMap;
    }

    public boolean isSplitting() {
        return splitting;
    }

    public void setSplitting(boolean splitting) {
        this.splitting = splitting;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public boolean isWatch() {
        return watch != null;
    }

    public void setWatch(boolean watch) {
        this.watch = watch ? "forever" : null;
    }

    public String getChunkNames() {
        return chunkNames;
    }

    public void setChunkNames(String chunkNames) {
        this.chunkNames = chunkNames;
    }

    public String getEntryNames() {
        return entryNames;
    }

    public void setEntryNames(String entryNames) {
        this.entryNames = entryNames;
    }

    public String getAssetNames() {
        return assetNames;
    }

    public void setAssetNames(String assetNames) {
        this.assetNames = assetNames;
    }

    public List<String> getExternal() {
        return external;
    }

    public void addExternal(String name) {
        external.add(name);
    }

    public String[] toParams() {
        final Field[] fields = EsBuildConfig.class.getDeclaredFields();
        List<String> result = new ArrayList<>(fields.length);
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                final Object value = field.get(this);
                if (value != null) {
                    final String fieldName = field.getName();
                    if (value == Boolean.TRUE) {
                        result.add("--" + fieldName.toLowerCase());
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

    private String convertField(String field) {
        final String[] split = field.split("(?=\\p{Upper})");
        return Arrays.stream(split).map(String::toLowerCase).collect(Collectors.joining("-"));
    }

    private static List<String> mapToString(String fieldName, Map<?, ?> map) {
        List<String> result = new ArrayList<>(map.size());
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            result.add("--%s:%s=%s".formatted(fieldName, entry.getKey(),
                    entry.getValue().toString().toLowerCase().replaceAll("_", "-")));
        }

        return result;
    }
}
