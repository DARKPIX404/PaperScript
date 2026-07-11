package dev.paperscript.host.api;

import dev.paperscript.host.PaperScriptPlugin;
import org.bukkit.command.CommandMap;
import org.graalvm.polyglot.Value;

import java.util.ArrayList;
import java.util.List;

public final class CommandsApi {
    private final PaperScriptPlugin plugin;
    private final List<ScriptCommand> registered = new ArrayList<>();

    public CommandsApi(PaperScriptPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(String name, Value handler) {
        register(name, handler, "", "/" + name);
    }

    public void register(String name, Value handler, String description, String usage) {
        if (handler == null || !handler.canExecute()) {
            throw new IllegalArgumentException("Command handler must be a function");
        }
        ScriptCommand cmd = new ScriptCommand(name, description, usage, handler, plugin);
        commandMap().register("paperscript", cmd);
        registered.add(cmd);
    }

    public void unregisterAll() {
        CommandMap map = commandMap();
        for (ScriptCommand cmd : registered) {
            cmd.unregister(map);
        }
        registered.clear();
    }

    private CommandMap commandMap() {
        return plugin.getServer().getCommandMap();
    }
}
