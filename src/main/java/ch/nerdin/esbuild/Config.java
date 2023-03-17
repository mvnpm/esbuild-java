package ch.nerdin.esbuild;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Config {
    private boolean bundle;

    private String entryPoint;
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

    enum Loader {
        BASE64, BINARY, COPY, CSS, DATAURL,
        EMPTY, FILE, JS, JSON, JSX, TEXT, TS, TSX
    }

    private Map<String, Loader> loader;

    private String outDir;
    private String packages;

    enum Platform {
        BROWSER, NODE, NEUTRAL
    }

    private Platform platform;

    private boolean serve;
    private boolean sourceMap;

    private boolean splitting;

    enum Target {
        ES2017, CHROME58, FIREFOX57,
        SAFARI11, EDGE16, NODE10, IE9, OPERA45
    }

    private Target target;

    private boolean watch;


    public boolean isBundle() {
        return bundle;
    }

    public void setBundle(boolean bundle) {
        this.bundle = bundle;
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(String entryPoint) {
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

    public String getOutDir() {
        return outDir;
    }

    public void setOutDir(String outDir) {
        this.outDir = outDir;
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
        return watch;
    }

    public void setWatch(boolean watch) {
        this.watch = watch;
    }

    protected String[] toParams() {
        final Field[] fields = Config.class.getDeclaredFields();
        List<String> result = new ArrayList<>(fields.length);
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                final Object value = field.get(this);
                if (value != null) {
                    final String fieldName = field.getName();
                    if (value == Boolean.TRUE) {
                        result.add("--" + fieldName.toLowerCase());
                    } else if (value instanceof Map) {
                        result.add("--" + fieldName + mapToString((Map<?, ?>) value));
                    } else if ("entryPoint".equals(field.getName())) {
                        result.add(value.toString());
                    } else if (!(value instanceof Boolean)) {
                        result.add("--%s=%s".formatted(fieldName.toLowerCase(), value.toString().toLowerCase()));
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return result.toArray(String[]::new);
    }

    private static String mapToString(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder(":");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue().toString().toLowerCase());
        }

        return sb.toString();
    }
}
