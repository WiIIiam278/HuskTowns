/*
 * This file is part of HuskTowns by William278. Do not redistribute!
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  All rights reserved.
 *
 *  This source code is provided as reference to licensed individuals that have purchased the HuskTowns
 *  plugin once from any of the official sources it is provided. The availability of this code does
 *  not grant you the rights to modify, re-distribute, compile or redistribute this source code or
 *  "plugin" outside this intended purpose. This license does not cover libraries developed by third
 *  parties that are utilised in the plugin.
 */

package net.william278.husktowns.claim;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Claim {

    @Expose
    private Chunk chunk;
    @Nullable
    @Expose
    private Type type;
    @Nullable
    @Expose
    @SerializedName("plot_members")
    private Map<UUID, Boolean> plotMembers;

    private Claim(@NotNull Chunk chunk, @NotNull Type type, @Nullable Map<UUID, Boolean> plotMembers) {
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

    public void setPlotMember(@NotNull UUID uuid, boolean manager) {
        if (getType() != Type.PLOT) {
            throw new IllegalStateException("Cannot add plot members to a " + getType().name() + "!");
        }
        if (plotMembers == null) {
            plotMembers = new HashMap<>();
        }
        plotMembers.put(uuid, manager);
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
        return plotMembers.containsKey(uuid);
    }

    public boolean isPlotManager(@NotNull UUID uuid) {
        if (getType() != Type.PLOT) {
            throw new IllegalStateException("Cannot check plot managers of a " + getType().name() + "!");
        }
        if (plotMembers == null) {
            return false;
        }
        return plotMembers.getOrDefault(uuid, false);
    }

    @NotNull
    public Chunk getChunk() {
        return chunk;
    }

    @NotNull
    public Type getType() {
        return type == null ? Type.CLAIM : type;
    }

    @Override
    public String toString() {
        return getType().name().toLowerCase() + " " + getChunk();
    }

    public boolean contains(Position position) {
        return getChunk().contains(position);
    }

    @NotNull
    public Set<UUID> getPlotMembers() {
        if (getType() != Type.PLOT) {
            throw new IllegalStateException("Cannot get plot members of a " + getType().name() + "!");
        }
        return plotMembers == null ? Set.of() : plotMembers.keySet();
    }

    public enum Type {

        CLAIM,
        FARM,
        PLOT;

        public static Optional<Type> fromId(@NotNull String id) {
            return Arrays.stream(values()).filter(type -> type.name().equalsIgnoreCase(id)).findFirst();
        }
    }
}
