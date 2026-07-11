package dev.paperscript.legacy.api;

import dev.paperscript.legacy.PaperScriptLegacyPlugin;
import dev.paperscript.legacy.engine.JsFunction;
import dev.paperscript.legacy.engine.NashornEngine;

import javax.script.ScriptEngine;
import java.nio.file.Path;

/** Root object bound to guest scripts as `ps`. Mirrors the modern facade. */
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

    private final ScriptEngine engine;
    private JsFunction onEnable;
    private JsFunction onDisable;

    public ScriptApi(PaperScriptLegacyPlugin plugin, ScriptEngine engine, String scriptName, Path dataDir) {
        this.engine = engine;
        this.logger = new LoggerApi(plugin.getLogger(), scriptName);
        this.events = new EventsApi(plugin, engine);
        this.commands = new CommandsApi(plugin, engine);
        this.scheduler = new SchedulerApi(plugin, engine);
        this.players = new PlayersApi(plugin);
        this.worlds = new WorldsApi(plugin);
        this.server = new ServerApi(plugin);
        this.loc = new LocationsApi();
        this.storage = new StorageApi(dataDir.resolve("storage").resolve(scriptName + ".json"), plugin.getLogger());
    }

    // JavaBean getters: Nashorn exposes them to guest code as properties
    // (ps.logger, ps.commands, ...), matching sdk/src/global.ts.
    public LoggerApi getLogger() { return logger; }
    public EventsApi getEvents() { return events; }
    public CommandsApi getCommands() { return commands; }
    public SchedulerApi getScheduler() { return scheduler; }
    public PlayersApi getPlayers() { return players; }
    public WorldsApi getWorlds() { return worlds; }
    public ServerApi getServer() { return server; }
    public LocationsApi getLoc() { return loc; }
    public StorageApi getStorage() { return storage; }

    /** Guest passes a JS function; it is adapted to a Java-callable proxy. */
    public void onEnable(Object fn) {
        this.onEnable = NashornEngine.adapt(engine, fn);
    }

    public void onDisable(Object fn) {
        this.onDisable = NashornEngine.adapt(engine, fn);
    }

    public void fireEnable() {
        if (onEnable != null) onEnable.call();
    }

    public void fireDisable() {
        if (onDisable != null) onDisable.call();
    }
}
