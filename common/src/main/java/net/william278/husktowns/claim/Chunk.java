/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.husktowns.claim;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.william278.cloplib.operation.OperationChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@NoArgsConstructor
public class Chunk implements OperationChunk {

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

    public static long asLong(int x, int z) {
        return ((long) x << 32) | (z & 0xffffffffL);
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

    /**
     * Get the long position of the chunk
     *
     * @return the long position
     */
    public long asLong() {
        return asLong(x, z);
    }

    public int distanceBetween(@NotNull Chunk chunk) {
        return Math.abs(x - chunk.x) + Math.abs(z - chunk.z);
    }

    public boolean contains(Position position) {
        return position.getX() >= x * 16 && position.getX() < (x + 1) * 16
            && position.getZ() >= z * 16 && position.getZ() < (z + 1) * 16;
    }
}
