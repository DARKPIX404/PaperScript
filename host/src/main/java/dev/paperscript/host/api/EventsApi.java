package dev.paperscript.host.api;

import dev.paperscript.host.PaperScriptPlugin;
import org.bukkit.entity.Player;
import org.graalvm.polyglot.Value;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public final class EventsApi {
    private final PaperScriptPlugin plugin;
    private final List<Value> joinHandlers = new CopyOnWriteArrayList<>();
    private final List<Value> quitHandlers = new CopyOnWriteArrayList<>();

    public EventsApi(PaperScriptPlugin plugin) {
        this.plugin = plugin;
    }

    public void onPlayerJoin(Value handler) {
        if (handler != null && handler.canExecute()) joinHandlers.add(handler);
    }

    public void onPlayerQuit(Value handler) {
        if (handler != null && handler.canExecute()) quitHandlers.add(handler);
    }

    public void dispatchJoin(Player player) {
        JoinEvent event = new JoinEvent(player);
        for (Value h : joinHandlers) invoke(h, event);
    }

    public void dispatchQuit(Player player) {
        QuitEvent event = new QuitEvent(player);
        for (Value h : quitHandlers) invoke(h, event);
    }

    private void invoke(Value handler, Object arg) {
        try {
            handler.executeVoid(arg);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Script event handler failed", ex);
        }
    }
}
