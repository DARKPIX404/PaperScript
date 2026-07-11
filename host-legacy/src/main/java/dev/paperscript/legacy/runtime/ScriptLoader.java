package dev.paperscript.legacy.runtime;

import dev.paperscript.legacy.PaperScriptLegacyPlugin;
import dev.paperscript.legacy.util.Json;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/** Scans the scripts folder, reads manifests and drives script lifecycles. */
public final class ScriptLoader {
    private final PaperScriptLegacyPlugin plugin;
    private final Path scriptsDir;
    private final Map<String, ScriptInstance> instances = new LinkedHashMap<>();

    public ScriptLoader(PaperScriptLegacyPlugin plugin, Path scriptsDir) {
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

        List<Path> dirs = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(scriptsDir)) {
            for (Path p : stream) {
                if (Files.isDirectory(p)) dirs.add(p);
            }
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Cannot read scripts dir: " + scriptsDir, ex);
            return;
        }
        for (Path dir : dirs) loadOne(dir);
    }

    public void loadOne(Path dir) {
        Path manifestFile = dir.resolve("plugin.json");
        if (!Files.exists(manifestFile)) {
            plugin.getLogger().warning("Skipping " + dir.getFileName() + ": no plugin.json");
            return;
        }
        String folderName = dir.getFileName().toString();
        try {
            ScriptManifest manifest = readManifest(manifestFile);
            if (manifest.name == null || manifest.name.trim().isEmpty()) manifest.name = folderName;
            if (manifest.main == null || manifest.main.trim().isEmpty()) manifest.main = "index.js";

            ScriptInstance existing = instances.get(manifest.name);
            if (existing != null) {
                existing.disable();
                instances.remove(manifest.name);
            }

            ScriptInstance instance = new ScriptInstance(plugin, manifest, dir);
            instance.enable();
            instances.put(manifest.name, instance);
            plugin.getLogger().info("Loaded script: " + manifest.name + " v" + manifest.version);
        } catch (Exception ex) {
            Errors.report(plugin.getLogger(), Level.SEVERE, "Load", folderName, ex);
        }
    }

    private ScriptManifest readManifest(Path file) throws IOException {
        String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        Map<String, Object> raw = Json.parseObject(text);
        ScriptManifest manifest = new ScriptManifest();
        manifest.name = stringOrNull(raw.get("name"));
        String version = stringOrNull(raw.get("version"));
        if (version != null) manifest.version = version;
        String main = stringOrNull(raw.get("main"));
        if (main != null) manifest.main = main;
        String apiVersion = stringOrNull(raw.get("apiVersion"));
        if (apiVersion != null) manifest.apiVersion = apiVersion;
        Object authors = raw.get("authors");
        if (authors instanceof List) {
            List<String> names = new ArrayList<>();
            for (Object a : (List<?>) authors) {
                if (a != null) names.add(String.valueOf(a));
            }
            manifest.authors = names.toArray(new String[0]);
        }
        return manifest;
    }

    private static String stringOrNull(Object value) {
        return value instanceof String ? (String) value : null;
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
        return Collections.unmodifiableSet(new LinkedHashSet<>(instances.keySet()));
    }

    public ScriptInstance get(String name) {
        return instances.get(name);
    }

    public Path scriptsDir() {
        return scriptsDir;
    }
}
