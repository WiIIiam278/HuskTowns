package net.william278.husktowns.claim;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Claim {

    @Expose
    private Chunk chunk;
    @Nullable
    @Expose
    private Type type;
    @Nullable
    @Expose
    @SerializedName("plot_members")
    private List<UUID> plotMembers;

    private Claim(@NotNull Chunk chunk, @NotNull Type type, @Nullable List<UUID> plotMembers) {
        this.chunk = chunk;
        this.type = type == Type.CLAIM ? null : type;
        this.plotMembers = type == Type.PLOT ? plotMembers : null;
    }

    @SuppressWarnings("unused")
    private Claim() {
    }

    @NotNull
    public static Claim at(@NotNull Chunk chunk) {
        return new Claim(chunk, Type.CLAIM, null);
    }

    public void setType(@NotNull Type type) {
        if (getType() == Type.PLOT && type != Type.PLOT) {
            plotMembers = null;
        }
        this.type = type == Type.CLAIM ? null : type;
    }

    public void addPlotMember(@NotNull UUID uuid) {
        if (getType() != Type.PLOT) {
            throw new IllegalStateException("Cannot add plot members to a " + getType().name() + "!");
        }
        if (plotMembers == null) {
            plotMembers = new ArrayList<>();
        }
        plotMembers.add(uuid);
    }

    public void removePlotMember(@NotNull UUID uuid) {
        if (getType() != Type.PLOT) {
            throw new IllegalStateException("Cannot remove plot members from a " + getType().name() + "!");
        }
        if (plotMembers == null) {
            return;
        }
        plotMembers.remove(uuid);
        if (plotMembers.isEmpty()) {
            plotMembers = null;
        }
    }

    public boolean isPlotMember(@NotNull UUID uuid) {
        if (getType() != Type.PLOT) {
            throw new IllegalStateException("Cannot check plot members of a " + getType().name() + "!");
        }
        if (plotMembers == null) {
            return false;
        }
        return plotMembers.contains(uuid);
    }

    @NotNull
    public Chunk getChunk() {
        return chunk;
    }

    @NotNull
    public Type getType() {
        return type == null ? Type.CLAIM : type;
    }

    public enum Type {

        CLAIM,
        FARM,
        PLOT

    }
}
