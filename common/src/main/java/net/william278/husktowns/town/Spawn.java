package net.william278.husktowns.town;

import net.william278.husktowns.claim.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Spawn {

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private World world;
    @Nullable
    private String server;

    private Spawn(double x, double y, double z, float yaw, float pitch, @NotNull World world, @Nullable String server) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = world;
        this.server = server;
    }

    public static Spawn of(double x, double y, double z, float yaw, float pitch, @NotNull World world, @Nullable String server) {
        return new Spawn(x, y, z, yaw, pitch, world, server);
    }

    @SuppressWarnings("unused")
    private Spawn() {
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

    @NotNull
    public World getWorld() {
        return world;
    }

    @Nullable
    public String getServer() {
        return server;
    }
}
