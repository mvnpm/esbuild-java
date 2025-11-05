package io.mvnpm.esbuild;

import io.mvnpm.esbuild.deno.ScriptLog;

public class BundlingException extends RuntimeException {

    private final ScriptLog logs;

    /**
     * Creates a BundleException with a message and optional logs.
     *
     * @param message The main exception message.
     * @param logs Additional logs (can be null).
     */
    public BundlingException(String message, ScriptLog logs) {
        super(message);
        this.logs = logs != null ? logs : new ScriptLog();
    }

    /**
     * Convenience constructor when there are no extra details.
     */
    public BundlingException(String message) {
        this(message, new ScriptLog());
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    public ScriptLog logs() {
        return logs;
    }
}
