package dev.paperscript.legacy.engine;

/**
 * Java-side handle to a guest JS function. Guest callbacks are adapted to this
 * interface via {@code Invocable.getInterface(fn, JsFunction.class)}.
 */
@FunctionalInterface
public interface JsFunction {
    Object call(Object... args);
}
