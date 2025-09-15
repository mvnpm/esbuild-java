package io.mvnpm.esbuild.resolve;

public class EsbuildResolutionException extends RuntimeException {
    public EsbuildResolutionException(String message) {
        super(message);
    }

    public EsbuildResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
