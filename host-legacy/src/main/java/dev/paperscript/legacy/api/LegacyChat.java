package dev.paperscript.legacy.api;

import org.bukkit.ChatColor;

/**
 * Legacy chat rendering: guest message strings use '&' color codes
 * (no Adventure/MiniMessage on 1.12.2-1.16.5).
 */
final class LegacyChat {
    private LegacyChat() {
    }

    static String render(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
