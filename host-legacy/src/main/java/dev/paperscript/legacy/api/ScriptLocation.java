package dev.paperscript.legacy.api;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/** Guest-facing location: a world name plus coordinates and rotation. */
public final class ScriptLocation {
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public ScriptLocation(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    static ScriptLocation of(Location loc) {
        String name = loc.getWorld() != null ? loc.getWorld().getName() : "world";
        return new ScriptLocation(name, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    /** Resolve to a Bukkit location, falling back to the given world if the named one is missing. Host-only. */
    Location resolve(World fallback) {
        World w = world != null ? Bukkit.getWorld(world) : null;
        if (w == null) w = fallback;
        return new Location(w, x, y, z, yaw, pitch);
    }
}
