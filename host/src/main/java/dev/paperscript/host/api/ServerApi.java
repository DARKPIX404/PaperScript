package dev.paperscript.host.api;

import dev.paperscript.host.PaperScriptPlugin;
import org.bukkit.Bukkit;

public final class ServerApi {
    public ServerApi(PaperScriptPlugin plugin) {
    }

    public String getVersion() {
        return Bukkit.getVersion();
    }

    public String getMinecraftVersion() {
        return Bukkit.getMinecraftVersion();
    }

    public int getOnlineCount() {
        return Bukkit.getOnlinePlayers().size();
    }

    public int getMaxPlayers() {
        return Bukkit.getMaxPlayers();
    }

    /** Broadcasts a MiniMessage-formatted message to the whole server. */
    public void broadcast(String message) {
        Bukkit.broadcast(Chat.parse(message));
    }
}
