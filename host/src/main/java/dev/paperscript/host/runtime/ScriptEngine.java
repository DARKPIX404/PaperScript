package dev.paperscript.host.runtime;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.ResourceLimits;
import org.graalvm.polyglot.io.IOAccess;

/**
 * Creates sandboxed GraalJS contexts for scripts.
 *
 * Notes:
 * - Only the curated facade is exposed to guest code. Host class lookup
 *   (Java.type), IO, native access and thread creation are disabled.
 * - A generous statement limit prevents runaway scripts. Exceeding it throws
 *   a ResourceLimitException for that context.
 * - GraalJS contexts are single-threaded: we only ever touch a context from
 *   the server main thread (onEnable, events, scheduled tasks all run there).
 */
public final class ScriptEngine {

    private static final long STATEMENT_LIMIT = 50_000_000L;

    public Context newContext() {
        ResourceLimits limits = ResourceLimits.newBuilder()
                .statementLimit(STATEMENT_LIMIT, null)
                .build();

        return Context.newBuilder("js")
                .allowHostAccess(HostAccess.SCOPED)
                .allowHostClassLookup(className -> false)
                .allowIO(IOAccess.NONE)
                .allowNativeAccess(false)
                .allowCreateThread(false)
                .resourceLimits(limits)
                .option("js.ecmascript-version", "latest")
                .build();
    }
}
