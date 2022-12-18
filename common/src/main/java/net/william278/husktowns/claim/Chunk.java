package net.william278.husktowns.claim;

import org.jetbrains.annotations.NotNull;

public class Chunk {

    private int x;
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
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Chunk chunk)) {
            return false;
        }
        return chunk.getX() == x && chunk.getZ() == z;
    }
}
