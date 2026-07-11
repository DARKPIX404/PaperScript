package dev.paperscript.host.api;

import java.util.logging.Logger;

public final class LoggerApi {
    private final Logger base;
    private final String prefix;

    public LoggerApi(Logger base, String scriptName) {
        this.base = base;
        this.prefix = "[" + scriptName + "] ";
    }

    public void info(String message) {
        base.info(prefix + message);
    }

    public void warn(String message) {
        base.warning(prefix + message);
    }

    public void error(String message) {
        base.severe(prefix + message);
    }
}
