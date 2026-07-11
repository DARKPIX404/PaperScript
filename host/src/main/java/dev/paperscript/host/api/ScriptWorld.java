package dev.paperscript.host.api;

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
        handle.setSpawnLocation(new Location(handle, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()));
    }
}
