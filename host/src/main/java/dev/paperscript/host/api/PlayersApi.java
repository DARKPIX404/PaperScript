package dev.paperscript.host.api;

import dev.paperscript.host.PaperScriptPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PlayersApi {
    public PlayersApi(PaperScriptPlugin plugin) {
    }

    public ScriptPlayer[] online() {
        return Bukkit.getOnlinePlayers().stream().map(ScriptPlayer::new).toArray(ScriptPlayer[]::new);
    }

    public ScriptPlayer get(String name) {
        Player p = Bukkit.getPlayerExact(name);
        return p == null ? null : new ScriptPlayer(p);
    }
}
