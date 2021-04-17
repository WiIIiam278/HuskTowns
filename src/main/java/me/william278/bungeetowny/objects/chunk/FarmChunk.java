package me.william278.bungeetowny.objects.chunk;

/**
 * A chunk that anyone can break melons, pumpkin, wheat & kill mobs within
 */
public class FarmChunk extends ClaimedChunk {

    public FarmChunk(String server, String worldName, int chunkX, int chunkZ) {
        super(server, worldName, chunkX, chunkZ);
    }
}
