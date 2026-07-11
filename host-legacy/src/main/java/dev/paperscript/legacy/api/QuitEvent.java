package dev.paperscript.legacy.api;

import org.bukkit.entity.Player;

public final class QuitEvent {
    private final ScriptPlayer player;

    public QuitEvent(Player player) {
        this.player = new ScriptPlayer(player);
    }

    public ScriptPlayer getPlayer() {
        return player;
    }
}
