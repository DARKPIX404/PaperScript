package dev.paperscript.legacy.api;

import dev.paperscript.legacy.PaperScriptLegacyPlugin;
import org.bukkit.Bukkit;

public final class ServerApi {
    public ServerApi(PaperScriptLegacyPlugin plugin) {
    }

    public String getVersion() {
        return Bukkit.getVersion();
    }

    public String getMinecraftVersion() {
        return Bukkit.getBukkitVersion();
    }

    public int getOnlineCount() {
        return Bukkit.getOnlinePlayers().size();
    }

    public int getMaxPlayers() {
        return Bukkit.getMaxPlayers();
    }

    /** Broadcasts a message with legacy '&' color codes to the whole server. */
    public void broadcast(String message) {
        Bukkit.broadcastMessage(LegacyChat.render(message));
    }
}
