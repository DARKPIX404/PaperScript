package dev.paperscript.host.runtime;

import com.google.gson.Gson;
import dev.paperscript.host.PaperScriptPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Stream;

/** Scans the scripts folder, reads manifests and drives script lifecycles. */
public final class ScriptLoader {
    private final PaperScriptPlugin plugin;
    private final Path scriptsDir;
    private final ScriptEngine engine = new ScriptEngine();
    private final Gson gson = new Gson();
    private final Map<String, ScriptInstance> instances = new LinkedHashMap<>();

    public ScriptLoader(PaperScriptPlugin plugin, Path scriptsDir) {
        this.plugin = plugin;
        this.scriptsDir = scriptsDir;
    }

    public void loadAll() {
        try {
            Files.createDirectories(scriptsDir);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Cannot create scripts dir: " + scriptsDir, ex);
            return;
        }

        try (Stream<Path> stream = Files.list(scriptsDir)) {
            stream.filter(Files::isDirectory).forEach(this::loadOne);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Cannot read scripts dir: " + scriptsDir, ex);
        }
    }

    public void loadOne(Path dir) {
        Path manifestFile = dir.resolve("plugin.json");
        if (!Files.exists(manifestFile)) {
            plugin.getLogger().warning("Skipping " + dir.getFileName() + ": no plugin.json");
            return;
        }
        String folderName = dir.getFileName().toString();
        try {
            ScriptManifest manifest = gson.fromJson(Files.readString(manifestFile), ScriptManifest.class);
            if (manifest.name == null || manifest.name.isBlank()) manifest.name = folderName;
            if (manifest.main == null || manifest.main.isBlank()) manifest.main = "index.js";

            ScriptInstance existing = instances.get(manifest.name);
            if (existing != null) {
                existing.disable();
                instances.remove(manifest.name);
            }

            ScriptInstance instance = new ScriptInstance(plugin, manifest, dir);
            instance.enable(engine);
            instances.put(manifest.name, instance);
            plugin.getLogger().info("Loaded script: " + manifest.name + " v" + manifest.version);
        } catch (Exception ex) {
            Errors.report(plugin.getLogger(), Level.SEVERE, "Load", folderName, ex);
        }
    }

    public void reload(String name) {
        ScriptInstance existing = instances.remove(name);
        if (existing != null) existing.disable();
        loadOne(scriptsDir.resolve(name));
    }

    public void disableAll() {
        for (ScriptInstance instance : instances.values()) {
            try {
                instance.disable();
            } catch (Exception ignored) {
            }
        }
        instances.clear();
    }

    public Collection<ScriptInstance> instances() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public Set<String> names() {
        return Collections.unmodifiableSet(instances.keySet());
    }

    public ScriptInstance get(String name) {
        return instances.get(name);
    }

    public Path scriptsDir() {
        return scriptsDir;
    }
}
