package net.william278.husktowns.town;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.william278.husktowns.claim.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Spawn {

    @Expose
    private double x;
    @Expose
    private double y;
    @Expose
    private double z;
    @Expose
    private float yaw;
    @Expose
    private float pitch;
    @Expose
    private World world;
    @Nullable
    @Expose
    private String server;
    @Expose
    @SerializedName("public")
    private boolean isPublic = false;

    private Spawn(double x, double y, double z, float yaw, float pitch, @NotNull World world, @Nullable String server) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = world;
        this.server = server;
    }

    @NotNull
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

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}
