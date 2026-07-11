package dev.paperscript.legacy.command;

import dev.paperscript.legacy.PaperScriptLegacyPlugin;
import dev.paperscript.legacy.runtime.ScriptInstance;
import dev.paperscript.legacy.runtime.ScriptLoader;
import dev.paperscript.legacy.runtime.ScriptManifest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class PaperScriptLegacyCommand implements CommandExecutor, TabCompleter {
    private final ScriptLoader loader;

    public PaperScriptLegacyCommand(PaperScriptLegacyPlugin plugin, ScriptLoader loader) {
        this.loader = loader;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("PaperScript. Usage: /ps <list|reload|info>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "list": {
                List<String> names = new ArrayList<>(loader.names());
                sender.sendMessage(names.isEmpty()
                        ? "No scripts loaded."
                        : "Scripts (" + names.size() + "): " + join(names, ", "));
                break;
            }
            case "reload": {
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
                break;
            }
            case "info": {
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
                break;
            }
            default:
                sender.sendMessage("Unknown subcommand. Usage: /ps <list|reload|info>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("list", "reload", "info");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("info"))) {
            return new ArrayList<>(loader.names());
        }
        return Collections.emptyList();
    }

    private static String join(List<String> parts, String sep) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) sb.append(sep);
            sb.append(parts.get(i));
        }
        return sb.toString();
    }
}
