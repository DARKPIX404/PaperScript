package dev.paperscript.legacy.api;

import dev.paperscript.legacy.util.Json;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tiny per-script key/value store backed by a JSON file. Values are stored as
 * JSON strings; guest code uses `JSON.stringify`/`JSON.parse`. Bound as
 * `ps.storage`. Dependency-free (no Gson on the legacy line).
 */
public final class StorageApi {
    private final Path file;
    private final Logger logger;
    private final Map<String, String> data = new LinkedHashMap<>();

    public StorageApi(Path file, Logger logger) {
        this.file = file;
        this.logger = logger;
        load();
    }

    private void load() {
        if (!Files.exists(file)) return;
        try {
            String json = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            Map<String, Object> loaded = Json.parseObject(json);
            for (Map.Entry<String, Object> e : loaded.entrySet()) {
                if (e.getValue() != null) data.put(e.getKey(), String.valueOf(e.getValue()));
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to read storage " + file, ex);
        }
    }

    public String get(String key) {
        return data.get(key);
    }

    public void set(String key, String jsonValue) {
        data.put(key, jsonValue);
        save();
    }

    public void remove(String key) {
        data.remove(key);
        save();
    }

    public void save() {
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, Json.stringify(data).getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Failed to write storage " + file, ex);
        }
    }
}
