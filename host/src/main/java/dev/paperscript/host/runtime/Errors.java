package dev.paperscript.host.runtime;

import org.graalvm.polyglot.PolyglotException;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Uniform, human-readable reporting of guest (JS) errors. */
public final class Errors {
    private Errors() {}

    public static void report(Logger log, Level level, String what, String script, Throwable ex) {
        if (ex instanceof PolyglotException pe && pe.isResourceExhausted()) {
            log.log(level, "Script '" + script + "' hit the statement limit during " + what
                    + " (possible infinite loop).");
            return;
        }
        String msg = ex == null ? "unknown error" : String.valueOf(ex.getMessage());
        log.log(level, what + " failed in '" + script + "': " + msg);
    }
}
