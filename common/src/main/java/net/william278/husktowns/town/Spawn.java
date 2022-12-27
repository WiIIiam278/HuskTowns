package net.william278.husktowns.town;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.william278.husktowns.claim.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @NotNull
    public static Spawn of(@NotNull Position position, @Nullable String server) {
        return new Spawn(position, server);
    }

    @SuppressWarnings("unused")
    private Spawn() {
    }

    @NotNull
    public Position getPosition() {
        return position;
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

    @Override
    public String toString() {
        return getPosition() + " (" + getServer() + ")";
    }
}
