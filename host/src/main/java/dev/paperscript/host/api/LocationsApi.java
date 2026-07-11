package dev.paperscript.host.api;

/** Factory for guest locations, bound as `ps.loc`. */
public final class LocationsApi {
    public ScriptLocation create(String world, double x, double y, double z) {
        return new ScriptLocation(world, x, y, z, 0f, 0f);
    }

    public ScriptLocation create(String world, double x, double y, double z, float yaw, float pitch) {
        return new ScriptLocation(world, x, y, z, yaw, pitch);
    }
}
