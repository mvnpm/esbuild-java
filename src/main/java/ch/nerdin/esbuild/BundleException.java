package ch.nerdin.esbuild;

public class BundleException extends RuntimeException {

    public BundleException(Exception cause) {
        super(cause);
    }
}
