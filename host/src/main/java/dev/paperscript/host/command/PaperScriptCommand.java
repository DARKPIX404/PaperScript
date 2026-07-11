package dev.paperscript.host.command;

import dev.paperscript.host.PaperScriptPlugin;
import dev.paperscript.host.runtime.ScriptInstance;
import dev.paperscript.host.runtime.ScriptLoader;
import dev.paperscript.host.runtime.ScriptManifest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class PaperScriptCommand implements CommandExecutor, TabCompleter {
    private final ScriptLoader loader;

    public PaperScriptCommand(PaperScriptPlugin plugin, ScriptLoader loader) {
        this.loader = loader;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length == 0) {
            sender.sendMessage("PaperScript. Usage: /ps <list|reload|info>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "list" -> {
                var names = loader.names();
                sender.sendMessage(names.isEmpty()
                        ? "No scripts loaded."
                        : "Scripts (" + names.size() + "): " + String.join(", ", names));
            }
            case "reload" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /ps reload <name>");
                    return true;
                }
                String name = args[1];
                long start = System.currentTimeMillis();
                loader.reload(name);
                ScriptInstance instance = loader.get(name);
                if (instance != null) {
                    sender.sendMessage("Reloaded '" + name + "' in " + (System.currentTimeMillis() - start) + "ms");
                } else {
                    sender.sendMessage("Script '" + name + "' not loaded (check console for errors).");
                }
            }
            case "info" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /ps info <name>");
                    return true;
                }
                ScriptInstance instance = loader.get(args[1]);
                if (instance == null) {
                    sender.sendMessage("No such script: " + args[1]);
                    return true;
                }
                ScriptManifest m = instance.manifest();
                sender.sendMessage(m.name + " v" + m.version + " (main: " + m.main + ", api: " + m.apiVersion + ")");
            }
            default -> sender.sendMessage("Unknown subcommand. Usage: /ps <list|reload|info>");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        if (args.length == 1) {
            return List.of("list", "reload", "info");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("info"))) {
            return new ArrayList<>(loader.names());
        }
        return List.of();
    }
}
