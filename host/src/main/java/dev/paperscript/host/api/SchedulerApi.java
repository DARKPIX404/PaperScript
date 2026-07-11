package dev.paperscript.host.api;

import dev.paperscript.host.PaperScriptPlugin;
import org.bukkit.Bukkit;
import org.graalvm.polyglot.Value;

import java.util.logging.Level;

public final class SchedulerApi {
    private final PaperScriptPlugin plugin;

    public SchedulerApi(PaperScriptPlugin plugin) {
        this.plugin = plugin;
    }

    public int runTask(Value fn) {
        return Bukkit.getScheduler().runTask(plugin, () -> safe(fn)).getTaskId();
    }

    public int runTaskLater(Value fn, long delayTicks) {
        return Bukkit.getScheduler().runTaskLater(plugin, () -> safe(fn), delayTicks).getTaskId();
    }

    public int runTaskTimer(Value fn, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> safe(fn), delayTicks, periodTicks).getTaskId();
    }

    public void cancelTask(int taskId) {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    private void safe(Value fn) {
        try {
            fn.executeVoid();
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Scheduled task failed", ex);
        }
    }
}
