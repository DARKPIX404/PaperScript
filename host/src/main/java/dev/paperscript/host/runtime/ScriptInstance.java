package dev.paperscript.host.runtime;

import dev.paperscript.host.PaperScriptPlugin;
import dev.paperscript.host.api.ScriptApi;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** One loaded script: its manifest, GraalJS context and bound facade. */
public final class ScriptInstance {
    private final PaperScriptPlugin plugin;
    private final ScriptManifest manifest;
    private final Path dir;

    private Context context;
    private ScriptApi api;

    public ScriptInstance(PaperScriptPlugin plugin, ScriptManifest manifest, Path dir) {
        this.plugin = plugin;
        this.manifest = manifest;
        this.dir = dir;
    }

    public void enable(ScriptEngine engine) throws IOException {
        Path mainFile = dir.resolve(manifest.main);
        if (!Files.exists(mainFile)) {
            throw new IOException("Main file not found: " + mainFile);
        }
        String code = Files.readString(mainFile);

        context = engine.newContext();
        api = new ScriptApi(plugin, name(), plugin.getDataFolder().toPath());
        context.getBindings("js").putMember("ps", api);

        Source source = Source.newBuilder("js", code, name() + "/" + manifest.main).build();
        context.eval(source);
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
        try {
            if (context != null) context.close();
        } catch (Exception ignored) {
        }
        context = null;
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
