package dev.paperscript.host.api;

import dev.paperscript.host.PaperScriptPlugin;
import org.graalvm.polyglot.Value;

import java.nio.file.Path;

/** Root object bound to guest scripts as `ps`. */
public final class ScriptApi {
    public final LoggerApi logger;
    public final EventsApi events;
    public final CommandsApi commands;
    public final SchedulerApi scheduler;
    public final PlayersApi players;
    public final WorldsApi worlds;
    public final ServerApi server;
    public final LocationsApi loc;
    public final StorageApi storage;

    private Value onEnable;
    private Value onDisable;

    public ScriptApi(PaperScriptPlugin plugin, String scriptName, Path dataDir) {
        this.logger = new LoggerApi(plugin.getLogger(), scriptName);
        this.events = new EventsApi(plugin);
        this.commands = new CommandsApi(plugin);
        this.scheduler = new SchedulerApi(plugin);
        this.players = new PlayersApi(plugin);
        this.worlds = new WorldsApi(plugin);
        this.server = new ServerApi(plugin);
        this.loc = new LocationsApi();
        this.storage = new StorageApi(dataDir.resolve("storage").resolve(scriptName + ".json"), plugin.getLogger());
    }

    public void onEnable(Value fn) {
        this.onEnable = fn;
    }

    public void onDisable(Value fn) {
        this.onDisable = fn;
    }

    public void fireEnable() {
        if (onEnable != null && onEnable.canExecute()) onEnable.executeVoid();
    }

    public void fireDisable() {
        if (onDisable != null && onDisable.canExecute()) onDisable.executeVoid();
    }
}
