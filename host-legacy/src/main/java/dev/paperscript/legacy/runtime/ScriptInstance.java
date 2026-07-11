package dev.paperscript.legacy.runtime;

import dev.paperscript.legacy.PaperScriptLegacyPlugin;
import dev.paperscript.legacy.api.ScriptApi;
import dev.paperscript.legacy.engine.NashornEngine;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** One loaded legacy script: its manifest, Nashorn engine and bound facade. */
public final class ScriptInstance {
    private final PaperScriptLegacyPlugin plugin;
    private final ScriptManifest manifest;
    private final Path dir;

    private ScriptEngine engine;
    private ScriptApi api;

    public ScriptInstance(PaperScriptLegacyPlugin plugin, ScriptManifest manifest, Path dir) {
        this.plugin = plugin;
        this.manifest = manifest;
        this.dir = dir;
    }

    public void enable() throws Exception {
        Path mainFile = dir.resolve(manifest.main);
        if (!Files.exists(mainFile)) {
            throw new IOException("Main file not found: " + mainFile);
        }
        String code = new String(Files.readAllBytes(mainFile), StandardCharsets.UTF_8);

        engine = NashornEngine.newEngine();
        api = new ScriptApi(plugin, engine, name(), plugin.getDataFolder().toPath());

        Bindings bindings = engine.createBindings();
        bindings.put("ps", api);
        bindings.put(ScriptEngine.FILENAME, name() + "/" + manifest.main);
        engine.eval(NashornEngine.PRELUDE, bindings);
        engine.eval(code, bindings);
        api.fireEnable();
    }

    public void disable() {
        try {
            if (api != null) api.fireDisable();
        } catch (Exception ex) {
            plugin.getLogger().warning("onDisable failed for " + name() + ": " + ex.getMessage());
        }
        try {
            if (api != null) api.storage.save();
        } catch (Exception ignored) {
        }
        try {
            if (api != null) api.commands.unregisterAll();
        } catch (Exception ignored) {
        }
        // Nashorn engines need no explicit close; drop references for GC.
        engine = null;
        api = null;
    }

    public String name() {
        return manifest.name;
    }

    public ScriptManifest manifest() {
        return manifest;
    }

    public ScriptApi api() {
        return api;
    }
}
