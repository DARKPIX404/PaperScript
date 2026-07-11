package dev.paperscript.legacy.api;

import dev.paperscript.legacy.PaperScriptLegacyPlugin;
import dev.paperscript.legacy.engine.JsFunction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public final class ScriptCommand extends Command {
    private final JsFunction handler;
    private final PaperScriptLegacyPlugin plugin;
    private boolean removed;

    protected ScriptCommand(String name, String description, String usage, JsFunction handler, PaperScriptLegacyPlugin plugin) {
        super(name);
        this.handler = handler;
        this.plugin = plugin;
        setDescription(description == null ? "" : description);
        setUsage(usage == null || usage.trim().isEmpty() ? "/" + name : usage);
    }

    void markRemoved() {
        this.removed = true;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (removed) {
            sender.sendMessage(LegacyChat.render("&cThis command is no longer registered."));
            return true;
        }
        try {
            handler.call(new CommandContext(sender, label, args));
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Script command /" + getName() + " failed", ex);
            sender.sendMessage(LegacyChat.render("&cAn error occurred while running this command."));
        }
        return true;
    }
}
