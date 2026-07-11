package dev.paperscript.legacy.api;

import dev.paperscript.legacy.PaperScriptLegacyPlugin;
import dev.paperscript.legacy.engine.JsFunction;
import dev.paperscript.legacy.engine.NashornEngine;
import org.bukkit.entity.Player;

import javax.script.ScriptEngine;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public final class EventsApi {
    private final PaperScriptLegacyPlugin plugin;
    private final ScriptEngine engine;
    private final List<JsFunction> joinHandlers = new CopyOnWriteArrayList<>();
    private final List<JsFunction> quitHandlers = new CopyOnWriteArrayList<>();

    public EventsApi(PaperScriptLegacyPlugin plugin, ScriptEngine engine) {
        this.plugin = plugin;
        this.engine = engine;
    }

    /** Guest passes a JS function; non-functions are ignored. */
    public void onPlayerJoin(Object handler) {
        JsFunction fn = NashornEngine.adapt(engine, handler);
        if (fn != null) joinHandlers.add(fn);
    }

    public void onPlayerQuit(Object handler) {
        JsFunction fn = NashornEngine.adapt(engine, handler);
        if (fn != null) quitHandlers.add(fn);
    }

    public void dispatchJoin(Player player) {
        JoinEvent event = new JoinEvent(player);
        for (JsFunction h : joinHandlers) invoke(h, event);
    }

    public void dispatchQuit(Player player) {
        QuitEvent event = new QuitEvent(player);
        for (JsFunction h : quitHandlers) invoke(h, event);
    }

    private void invoke(JsFunction handler, Object arg) {
        try {
            handler.call(arg);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Script event handler failed", ex);
        }
    }
}
