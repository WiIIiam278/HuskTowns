package net.william278.husktowns.town;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.william278.husktowns.claim.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a town's spawn position
 */
public class Spawn {

    @Expose
    private Position position;
    @Nullable
    @Expose
    private String server;
    @Expose
    @SerializedName("public")
    private boolean isPublic = false;

    private Spawn(@NotNull Position position, @Nullable String server) {
        this.position = position;
        this.server = server;
    }

    /**
     * Create a town spawn from a position and server
     *
     * @param position the {@link Position} of the spawn
     * @param server   the ID of the server the spawn is on
     * @return the spawn
     */
    @NotNull
    public static Spawn of(@NotNull Position position, @Nullable String server) {
        return new Spawn(position, server);
    }

    @SuppressWarnings("unused")
    private Spawn() {
    }

    /**
     * Get the position of the spawn
     *
     * @return the position of the spawn
     */
    @NotNull
    public Position getPosition() {
        return position;
    }

    /**
     * Get the ID of the server the spawn is on
     *
     * @return the ID of the server the spawn is on
     */
    @Nullable
    public String getServer() {
        return server;
    }

    /**
     * Check if the spawn is public
     *
     * @return {@code true} if the spawn is public, {@code false} otherwise
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * Set the spawn privacy
     *
     * @param isPublic {@code true} if the spawn is public, {@code false} otherwise
     */
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    @Override
    public String toString() {
        return getPosition() + " (" + getServer() + ")";
    }
}
