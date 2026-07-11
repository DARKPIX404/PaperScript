package dev.paperscript.host.api;

import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

/** Slim, safe view of a Bukkit player exposed to guest scripts. */
public final class ScriptPlayer {
    private final Player handle;

    public ScriptPlayer(Player handle) {
        this.handle = handle;
    }

    public String getName() {
        return handle.getName();
    }

    public String getUniqueId() {
        return handle.getUniqueId().toString();
    }

    public boolean isOnline() {
        return handle.isOnline();
    }

    public double getHealth() {
        return handle.getHealth();
    }

    public void setHealth(double health) {
        handle.setHealth(health);
    }

    public int getFoodLevel() {
        return handle.getFoodLevel();
    }

    public void setFoodLevel(int level) {
        handle.setFoodLevel(level);
    }

    /** Sends a MiniMessage-formatted message (`<red>`, `<gradient:..>`, `<bold>`, hover/click, ...). */
    public void sendMessage(String message) {
        handle.sendMessage(Chat.parse(message));
    }

    public ScriptLocation getLocation() {
        return ScriptLocation.of(handle.getLocation());
    }

    public boolean teleport(ScriptLocation loc) {
        if (loc == null) return false;
        return handle.teleport(loc.resolve(handle.getWorld()));
    }

    public void heal() {
        AttributeInstance max = handle.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        handle.setHealth(max != null ? max.getValue() : 20.0);
        handle.setFoodLevel(20);
        handle.setSaturation(5.0f);
        handle.setFireTicks(0);
    }

    public void feed() {
        handle.setFoodLevel(20);
        handle.setSaturation(10.0f);
    }

    public boolean getAllowFlight() {
        return handle.getAllowFlight();
    }

    public void setAllowFlight(boolean allow) {
        handle.setAllowFlight(allow);
    }

    public boolean isFlying() {
        return handle.isFlying();
    }

    public void setFlying(boolean flying) {
        handle.setFlying(flying);
    }

    public String getGameMode() {
        return handle.getGameMode().name().toLowerCase();
    }

    public boolean setGameMode(String mode) {
        if (mode == null) return false;
        try {
            handle.setGameMode(GameMode.valueOf(mode.trim().toUpperCase()));
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    // Package-private on purpose: exposes the raw Bukkit Player to host code only.
    // Guest code must never reach it (HostAccess.ALL exposes only PUBLIC members),
    // otherwise scripts could escape the facade and call arbitrary Bukkit methods.
    Player handle() {
        return handle;
    }
}
