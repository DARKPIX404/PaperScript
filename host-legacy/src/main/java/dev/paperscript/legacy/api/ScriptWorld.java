package dev.paperscript.legacy.api;

import org.bukkit.Location;
import org.bukkit.World;

/** Slim view of a world exposed to guest scripts. */
public final class ScriptWorld {
    private final World handle;

    public ScriptWorld(World handle) {
        this.handle = handle;
    }

    public String getName() {
        return handle.getName();
    }

    public long getTime() {
        return handle.getTime();
    }

    public void setTime(long time) {
        handle.setTime(time);
    }

    public int getPlayerCount() {
        return handle.getPlayers().size();
    }

    public ScriptLocation getSpawnLocation() {
        return ScriptLocation.of(handle.getSpawnLocation());
    }

    public void setSpawnLocation(ScriptLocation loc) {
        if (loc == null) return;
        if (loc.getWorld() != null && !loc.getWorld().equals(handle.getName())) {
            // Spawn belongs to another world; ignore to avoid surprising cross-world writes.
            return;
        }
        // World#setSpawnLocation(int,int,int) exists on 1.12.2 (the Location
        // overload arrived in 1.13) — use block coordinates for compatibility.
        handle.setSpawnLocation(
                (int) Math.floor(loc.getX()),
                (int) Math.floor(loc.getY()),
                (int) Math.floor(loc.getZ()));
    }

    // Host-only convenience (used by facade code that needs a Bukkit Location).
    Location spawnAsLocation() {
        return handle.getSpawnLocation();
    }
}
