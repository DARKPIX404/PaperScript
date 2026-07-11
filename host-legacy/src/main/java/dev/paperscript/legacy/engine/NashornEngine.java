package dev.paperscript.legacy.engine;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.lang.reflect.Method;

/**
 * Creates per-script Nashorn engines for the legacy line (servers running
 * Java 8-14, i.e. Minecraft 1.12.2-1.16.5).
 *
 * Notes:
 * - The Nashorn API ({@code jdk.nashorn.api.scripting.*}) is not on the JDK 15+
 *   compile classpath, so the factory is reached by reflection. On a Java 8-14
 *   runtime this yields an engine with ES6 enabled and Java access disabled.
 * - Sandbox: the engine is created with {@code --no-java} (no Java.type /
 *   Packages / class lookup) and {@link #PRELUDE} deletes the remaining host
 *   globals (load/quit/$EXEC/...) as defense in depth. Bound facade objects
 *   still expose their public members to guest code.
 * - Nashorn has NO statement/time limit (unlike GraalJS ResourceLimits).
 *   See docs/security.md.
 */
public final class NashornEngine {
    private NashornEngine() {
    }

    public static ScriptEngine newEngine() {
        try {
            Class<?> factoryClass = Class.forName("jdk.nashorn.api.scripting.NashornScriptEngineFactory");
            Object factory = factoryClass.getDeclaredConstructor().newInstance();
            Method create = factoryClass.getMethod("getScriptEngine", String[].class);
            return (ScriptEngine) create.invoke(factory, (Object) new String[]{"--language=es6", "--no-java"});
        } catch (ClassNotFoundException notFound) {
            // Fallback: plain lookup (ES5.1, Java globals removed by PRELUDE only).
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            if (engine == null) {
                throw new IllegalStateException(
                        "Nashorn JS engine is not available. The legacy line requires a Java 8-14 runtime.");
            }
            return engine;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create Nashorn engine: " + ex, ex);
        }
    }

    /** Adapts a JS function object passed by guest code into a Java-callable proxy. */
    public static JsFunction adapt(ScriptEngine engine, Object fn) {
        if (fn == null || !(engine instanceof Invocable)) return null;
        try {
            return ((Invocable) engine).getInterface(fn, JsFunction.class);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Evaluated in every script's bindings before guest code. With
     * {@code --no-java} the Java globals are already absent; this removes the
     * rest of the host escape hatches.
     */
    public static final String PRELUDE =
            "delete this.Java;delete this.Packages;delete this.JavaImporter;"
          + "delete this.java;delete this.javax;delete this.org;delete this.com;"
          + "delete this.edu;delete this.net;delete this.load;delete this.loadWithNewGlobal;"
          + "delete this.quit;delete this.exit;delete this.$EXEC;delete this.$ENV;"
          + "delete this.$OUT;delete this.$ERR;delete this.$EXIT;"
          + "delete this.readFully;delete this.readLine;";
}
