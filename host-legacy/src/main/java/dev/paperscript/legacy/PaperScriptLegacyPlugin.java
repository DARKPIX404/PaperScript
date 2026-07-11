package dev.paperscript.legacy;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Legacy (1.12.2-1.16.5, Java 8/11) host — foundation stub for v1.
 * The Nashorn-based JS engine and the shared facade will land in v1.1
 * (see docs/legacy.md).
 */
public final class PaperScriptLegacyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("PaperScript Legacy loaded (foundation stub). Nashorn JS engine arrives in v1.1.");
    }

    @Override
    public void onDisable() {
        getLogger().info("PaperScript Legacy disabled.");
    }
}
