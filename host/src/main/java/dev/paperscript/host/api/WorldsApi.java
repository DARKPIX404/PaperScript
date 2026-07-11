package dev.paperscript.host.api;

import dev.paperscript.host.PaperScriptPlugin;
import org.bukkit.Bukkit;

public final class WorldsApi {
    public WorldsApi(PaperScriptPlugin plugin) {
    }

    public ScriptWorld[] all() {
        return Bukkit.getWorlds().stream().map(ScriptWorld::new).toArray(ScriptWorld[]::new);
    }

    public ScriptWorld get(String name) {
        var w = Bukkit.getWorld(name);
        return w == null ? null : new ScriptWorld(w);
    }
}
