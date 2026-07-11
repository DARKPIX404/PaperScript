package dev.paperscript.host;

import dev.paperscript.host.command.PaperScriptCommand;
import dev.paperscript.host.runtime.EventBridge;
import dev.paperscript.host.runtime.ScriptLoader;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public final class PaperScriptPlugin extends JavaPlugin {

    private ScriptLoader scriptLoader;

    @Override
    public void onEnable() {
        Path scriptsDir = getDataFolder().toPath().resolve("scripts");
        this.scriptLoader = new ScriptLoader(this, scriptsDir);
        scriptLoader.loadAll();

        getServer().getPluginManager().registerEvents(new EventBridge(this, scriptLoader), this);

        var ps = getCommand("ps");
        if (ps != null) {
            var cmd = new PaperScriptCommand(this, scriptLoader);
            ps.setExecutor(cmd);
            ps.setTabCompleter(cmd);
        }

        getLogger().info("PaperScript enabled. Scripts dir: " + scriptsDir.toAbsolutePath());
    }

    @Override
    public void onDisable() {
        if (scriptLoader != null) {
            scriptLoader.disableAll();
        }
    }
}
