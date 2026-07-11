package dev.paperscript.host.runtime;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** Bridges Bukkit events to every loaded script instance. */
public final class EventBridge implements Listener {
    private final ScriptLoader loader;

    public EventBridge(ScriptLoader loader) {
        this.loader = loader;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        for (ScriptInstance instance : loader.instances()) {
            instance.api().events.dispatchJoin(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        for (ScriptInstance instance : loader.instances()) {
            instance.api().events.dispatchQuit(event.getPlayer());
        }
    }
}
