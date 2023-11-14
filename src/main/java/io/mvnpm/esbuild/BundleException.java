package io.mvnpm.esbuild;

public class BundleException extends RuntimeException {

    private final String output;

    public BundleException(String message, String output) {
        super(message);
        this.output = output;
    }

    public String output() {
        return output;
    }
}
