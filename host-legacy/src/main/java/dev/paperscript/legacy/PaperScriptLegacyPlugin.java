package dev.paperscript.legacy;

import dev.paperscript.legacy.command.PaperScriptLegacyCommand;
import dev.paperscript.legacy.runtime.EventBridge;
import dev.paperscript.legacy.runtime.ScriptLoader;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

/**
 * Legacy (1.12.2-1.16.5, Java 8/11) PaperScript host. Runs guest scripts on a
 * sandboxed Nashorn engine with the same `ps.*` facade as the modern host
 * (see docs/legacy.md, docs/security.md).
 */
public final class PaperScriptLegacyPlugin extends JavaPlugin {

    private ScriptLoader scriptLoader;

    @Override
    public void onEnable() {
        Path scriptsDir = getDataFolder().toPath().resolve("scripts");
        this.scriptLoader = new ScriptLoader(this, scriptsDir);
        scriptLoader.loadAll();

        getServer().getPluginManager().registerEvents(new EventBridge(this, scriptLoader), this);

        PluginCommand ps = getCommand("ps");
        if (ps != null) {
            PaperScriptLegacyCommand cmd = new PaperScriptLegacyCommand(this, scriptLoader);
            ps.setExecutor(cmd);
            ps.setTabCompleter(cmd);
        }

        getLogger().info("PaperScript Legacy enabled (Nashorn). Scripts dir: " + scriptsDir.toAbsolutePath());
    }

    @Override
    public void onDisable() {
        if (scriptLoader != null) {
            scriptLoader.disableAll();
        }
    }
}
