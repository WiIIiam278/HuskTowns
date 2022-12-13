package net.william278.husktowns.claim;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Claim {

    private Position position;
    @Nullable
    private Type type;
    @Nullable
    private List<UUID> plotMembers;

    private Claim(@NotNull Position position, @NotNull Type type, @Nullable List<UUID> plotMembers) {
        this.position = position;
        this.type = type == Type.CLAIM ? null : type;
        this.plotMembers = type == Type.PLOT ? plotMembers : null;
    }

    @SuppressWarnings("unused")
    private Claim() {
    }

    @NotNull
    public static Claim at(@NotNull Position position) {
        return new Claim(position, Type.CLAIM, null);
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
    public Position getPosition() {
        return position;
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
