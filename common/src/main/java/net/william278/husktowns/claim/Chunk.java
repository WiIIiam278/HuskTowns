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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Chunk {

    @Expose
    private int x;
    @Expose
    private int z;

    private Chunk(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @NotNull
    public static Chunk at(int x, int z) {
        return new Chunk(x, z);
    }

    @SuppressWarnings("unused")
    private Chunk() {
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        final Chunk chunk = (Chunk) obj;
        return x == chunk.x && z == chunk.z;
    }

    @Override
    public String toString() {
        return "(x: " + x + ", z: " + z + ")";
    }

    public int distanceBetween(@NotNull Chunk chunk) {
        return Math.abs(x - chunk.x) + Math.abs(z - chunk.z);
    }

    public boolean contains(Position position) {
        return position.getX() >= x * 16 && position.getX() < (x + 1) * 16
               && position.getZ() >= z * 16 && position.getZ() < (z + 1) * 16;
    }
}
