package dev.paperscript.legacy.api;

import dev.paperscript.legacy.PaperScriptLegacyPlugin;
import dev.paperscript.legacy.engine.JsFunction;
import dev.paperscript.legacy.engine.NashornEngine;
import org.bukkit.Bukkit;

import javax.script.ScriptEngine;
import java.util.logging.Level;

public final class SchedulerApi {
    private final PaperScriptLegacyPlugin plugin;
    private final ScriptEngine engine;

    public SchedulerApi(PaperScriptLegacyPlugin plugin, ScriptEngine engine) {
        this.plugin = plugin;
        this.engine = engine;
    }

    public int runTask(Object fn) {
        return Bukkit.getScheduler().runTask(plugin, () -> safe(adapt(fn))).getTaskId();
    }

    public int runTaskLater(Object fn, long delayTicks) {
        return Bukkit.getScheduler().runTaskLater(plugin, () -> safe(adapt(fn)), delayTicks).getTaskId();
    }

    public int runTaskTimer(Object fn, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> safe(adapt(fn)), delayTicks, periodTicks).getTaskId();
    }

    public void cancelTask(int taskId) {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    private JsFunction adapt(Object fn) {
        JsFunction adapted = NashornEngine.adapt(engine, fn);
        if (adapted == null) throw new IllegalArgumentException("Scheduled task must be a function");
        return adapted;
    }

    private void safe(JsFunction fn) {
        try {
            fn.call();
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Scheduled task failed", ex);
        }
    }
}
