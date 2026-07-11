package dev.paperscript.host.api;

import org.bukkit.command.CommandSender;

/** Slim view of a command sender (player or console) exposed to guest scripts. */
public final class ScriptSender {
    private final CommandSender handle;

    public ScriptSender(CommandSender handle) {
        this.handle = handle;
    }

    public String getName() {
        return handle.getName();
    }

    public boolean isOp() {
        return handle.isOp();
    }

    public boolean isPlayer() {
        return handle instanceof org.bukkit.entity.Player;
    }

    /** Sends a MiniMessage-formatted message. */
    public void sendMessage(String message) {
        handle.sendMessage(Chat.parse(message));
    }
}
