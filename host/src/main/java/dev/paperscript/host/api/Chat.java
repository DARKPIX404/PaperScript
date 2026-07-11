package dev.paperscript.host.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

/** Parses guest message strings as MiniMessage, falling back to plain text. */
final class Chat {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private Chat() {
    }

    static Component parse(String text) {
        if (text == null) return Component.empty();
        try {
            return MM.deserialize(text);
        } catch (Exception ignored) {
            return Component.text(text);
        }
    }
}
