package dev.paperscript.host.api;

import org.bukkit.entity.Player;

public final class JoinEvent {
    private final ScriptPlayer player;

    public JoinEvent(Player player) {
        this.player = new ScriptPlayer(player);
    }

    public ScriptPlayer getPlayer() {
        return player;
    }
}
