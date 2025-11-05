package io.mvnpm.esbuild.deno;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import io.mvnpm.esbuild.script.DevScript;

public class ScriptLog {
    private static final Logger LOG = Logger.getLogger(DevScript.class);
    private static final Pattern LOG_PATTERN = Pattern.compile("^.*?\\[(\\w+)]\\s*(.*)$");
    private final List<LogMessage> messages = new ArrayList<>();

    public record LogMessage(Logger.Level level, String message) {
        @Override
        public String toString() {
            return "[" + level + "] " + message;
        }
    }

    public void add(String raw) {
        if (raw == null || raw.isBlank())
            return;

        Matcher m = LOG_PATTERN.matcher(raw.trim());
        Logger.Level level = Logger.Level.INFO;
        String message = raw.trim();

        if (m.matches()) {
            try {
                level = Logger.Level.valueOf(m.group(1).toUpperCase());
                message = m.group(2);
            } catch (IllegalArgumentException ignored) {
                // fallback to INFO
            }
        }

        messages.add(new LogMessage(level, message.replace("<br>", "\n")));
    }

    public void logAll() {
        for (LogMessage message : messages) {
            LOG.log(message.level, message.message);
        }
    }

    @Override
    public String toString() {
        return messages.stream().map(LogMessage::message).collect(Collectors.joining("\n"));
    }

    public void clear() {
        messages.clear();
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public List<String> errors() {
        return messages.stream().filter(m -> m.level == Logger.Level.ERROR).map(LogMessage::message).toList();
    }

    public long countErrors() {
        return messages.stream().filter(m -> m.level == Logger.Level.ERROR).count();
    }

    public long countWarnings() {
        return messages.stream().filter(m -> m.level == Logger.Level.WARN).count();
    }
}
