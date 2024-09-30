package io.mvnpm.esbuild.install;

public class InstallException extends RuntimeException {

    private final String id;

    public InstallException(String message, String id) {
        super(message);
        this.id = id;
    }

    public String id() {
        return id;
    }
}
