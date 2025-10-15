package io.mvnpm.esbuild;

public class BundleException extends RuntimeException {

    private final String details;

    /**
     * Creates a BundleException with a message and optional details.
     *
     * @param message The main exception message.
     * @param details Additional details (can be null or empty).
     */
    public BundleException(String message, String details) {
        super(message);
        this.details = details != null ? details : "";
    }

    /**
     * Convenience constructor when there are no extra details.
     */
    public BundleException(String message) {
        this(message, "");
    }

    @Override
    public String getMessage() {
        if (details.isEmpty()) {
            return super.getMessage();
        }
        return super.getMessage() + ":\n" + details;
    }

    public String details() {
        return details;
    }
}
