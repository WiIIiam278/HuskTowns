package net.william278.husktowns.claim;

import org.jetbrains.annotations.NotNull;

public class Position {

    private int x;
    private int z;

    private Position(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @NotNull
    public static Position at(int x, int z) {
        return new Position(x, z);
    }

    @SuppressWarnings("unused")
    private Position() {
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
        if (!(obj instanceof Position position)) {
            return false;
        }
        return position.getX() == x && position.getZ() == z;
    }
}
