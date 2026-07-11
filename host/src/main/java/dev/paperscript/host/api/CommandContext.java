package dev.paperscript.host.api;

import org.bukkit.command.CommandSender;

public final class CommandContext {
    private final ScriptSender sender;
    private final String label;
    private final String[] args;

    public CommandContext(CommandSender sender, String label, String[] args) {
        this.sender = new ScriptSender(sender);
        this.label = label;
        this.args = args.clone();
    }

    public ScriptSender getSender() {
        return sender;
    }

    public String getLabel() {
        return label;
    }

    public String[] getArgs() {
        return args.clone();
    }

    public String arg(int index) {
        return index >= 0 && index < args.length ? args[index] : null;
    }
}
