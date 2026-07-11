package dev.paperscript.legacy.api;

import dev.paperscript.legacy.PaperScriptLegacyPlugin;
import dev.paperscript.legacy.engine.JsFunction;
import dev.paperscript.legacy.engine.NashornEngine;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import javax.script.ScriptEngine;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CommandsApi {
    private final PaperScriptLegacyPlugin plugin;
    private final ScriptEngine engine;
    private final List<ScriptCommand> registered = new ArrayList<>();

    public CommandsApi(PaperScriptLegacyPlugin plugin, ScriptEngine engine) {
        this.plugin = plugin;
        this.engine = engine;
    }

    public void register(String name, Object handler) {
        register(name, handler, "", "/" + name);
    }

    /** Guest passes a JS function as `handler`; it must adapt to JsFunction. */
    public void register(String name, Object handler, String description, String usage) {
        JsFunction fn = NashornEngine.adapt(engine, handler);
        if (fn == null) {
            throw new IllegalArgumentException("Command handler must be a function");
        }
        ScriptCommand cmd = new ScriptCommand(name, description, usage, fn, plugin);
        commandMap().register("paperscriptlegacy", cmd);
        registered.add(cmd);
    }

    public void unregisterAll() {
        CommandMap map = commandMap();
        for (ScriptCommand cmd : registered) {
            cmd.markRemoved();
            removeFromMap(map, cmd.getName());
        }
        registered.clear();
    }

    /**
     * {@code Server.getCommandMap()} is not on the 1.12.2 API interface; the
     * implementation (CraftServer) exposes it, so reach it by reflection — the
     * canonical pre-1.13 approach.
     */
    private CommandMap commandMap() {
        try {
            Method m = plugin.getServer().getClass().getMethod("getCommandMap");
            return (CommandMap) m.invoke(plugin.getServer());
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot access the Bukkit CommandMap", ex);
        }
    }

    /**
     * SimpleCommandMap keeps commands in a private `knownCommands` map; there is
     * no public unregister on 1.12.2, so remove by reflection (best effort).
     */
    @SuppressWarnings("unchecked")
    private static void removeFromMap(CommandMap map, String name) {
        try {
            Field field = map.getClass().getDeclaredField("knownCommands");
            field.setAccessible(true);
            Map<String, Command> known = (Map<String, Command>) field.get(map);
            known.remove(name.toLowerCase());
            known.remove("paperscriptlegacy:" + name.toLowerCase());
        } catch (Exception ignored) {
        }
    }
}
