package dev.paperscript.legacy.runtime;

import javax.script.ScriptException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Uniform, human-readable reporting of guest (JS) errors on the legacy line. */
public final class Errors {
    private Errors() {
    }

    public static void report(Logger log, Level level, String what, String script, Throwable ex) {
        ScriptException se = unwrap(ex);
        if (se != null) {
            StringBuilder loc = new StringBuilder();
            if (se.getFileName() != null) loc.append(se.getFileName());
            if (se.getLineNumber() >= 0) loc.append(":").append(se.getLineNumber());
            if (se.getColumnNumber() >= 0) loc.append(":").append(se.getColumnNumber());
            log.log(level, what + " failed in '" + script + "'"
                    + (loc.length() > 0 ? " (" + loc + ")" : "") + ": " + se.getMessage());
            return;
        }
        String msg = ex == null ? "unknown error" : String.valueOf(ex.getMessage());
        log.log(level, what + " failed in '" + script + "': " + msg);
    }

    /** Handler proxies wrap ScriptException in RuntimeException — dig it out. */
    private static ScriptException unwrap(Throwable t) {
        while (t != null) {
            if (t instanceof ScriptException) return (ScriptException) t;
            t = t.getCause();
        }
        return null;
    }
}
