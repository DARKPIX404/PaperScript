package dev.paperscript.legacy.api;

import dev.paperscript.legacy.PaperScriptLegacyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public final class WorldsApi {
    public WorldsApi(PaperScriptLegacyPlugin plugin) {
    }

    public ScriptWorld[] all() {
        List<ScriptWorld> out = new ArrayList<>();
        for (World w : Bukkit.getWorlds()) {
            out.add(new ScriptWorld(w));
        }
        return out.toArray(new ScriptWorld[0]);
    }

    public ScriptWorld get(String name) {
        World w = Bukkit.getWorld(name);
        return w == null ? null : new ScriptWorld(w);
    }
}
