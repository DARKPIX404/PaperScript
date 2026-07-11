package dev.paperscript.legacy.runtime;

import dev.paperscript.legacy.PaperScriptLegacyPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.logging.Level;

/** Bridges Bukkit events to every loaded script instance. */
public final class EventBridge implements Listener {
    private final PaperScriptLegacyPlugin plugin;
    private final ScriptLoader loader;

    public EventBridge(PaperScriptLegacyPlugin plugin, ScriptLoader loader) {
        this.plugin = plugin;
        this.loader = loader;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        for (ScriptInstance instance : loader.instances()) {
            try {
                instance.api().events.dispatchJoin(event.getPlayer());
            } catch (Exception ex) {
                Errors.report(plugin.getLogger(), Level.WARNING, "player.join", instance.name(), ex);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        for (ScriptInstance instance : loader.instances()) {
            try {
                instance.api().events.dispatchQuit(event.getPlayer());
            } catch (Exception ex) {
                Errors.report(plugin.getLogger(), Level.WARNING, "player.quit", instance.name(), ex);
            }
        }
    }
}
