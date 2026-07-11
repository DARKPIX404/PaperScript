package dev.paperscript.host.api;

import dev.paperscript.host.PaperScriptPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public final class ScriptCommand extends Command {
    private final Value handler;
    private final PaperScriptPlugin plugin;

    protected ScriptCommand(String name, String description, String usage, Value handler, PaperScriptPlugin plugin) {
        super(name);
        this.handler = handler;
        this.plugin = plugin;
        setDescription(description == null ? "" : description);
        setUsage(usage == null || usage.isBlank() ? "/" + name : usage);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, String @NotNull [] args) {
        try {
            handler.executeVoid(new CommandContext(sender, label, args));
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Script command /" + getName() + " failed", ex);
            sender.sendMessage("An error occurred while running this command.");
        }
        return true;
    }
}
