package dev.paperscript.legacy.api;

import dev.paperscript.legacy.PaperScriptLegacyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class PlayersApi {
    public PlayersApi(PaperScriptLegacyPlugin plugin) {
    }

    public ScriptPlayer[] online() {
        List<ScriptPlayer> out = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            out.add(new ScriptPlayer(p));
        }
        return out.toArray(new ScriptPlayer[0]);
    }

    public ScriptPlayer get(String name) {
        Player p = Bukkit.getPlayerExact(name);
        return p == null ? null : new ScriptPlayer(p);
    }
}
