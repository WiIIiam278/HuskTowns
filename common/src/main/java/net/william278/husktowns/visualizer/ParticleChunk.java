package net.william278.husktowns.visualizer;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.World;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ParticleChunk {

    private final Position[] positions = new Position[4];

    private ParticleChunk(@NotNull Chunk chunk, @NotNull World world, @NotNull HuskTowns plugin) {
        super();
        positions[0] = Position.at(chunk.getX() * 16, 64, chunk.getZ() * 16, world);
        positions[1] = Position.at(chunk.getX() * 16, 64, (chunk.getZ() * 16) + 15, world);
        positions[2] = Position.at((chunk.getX() * 16) + 15, 64, (chunk.getZ() * 16) + 15, world);
        positions[3] = Position.at((chunk.getX() * 16) + 15, 64, chunk.getZ() * 16, world);
        Arrays.stream(positions).forEach(position -> position.setY(plugin.getHighestBlockAt(position)));
    }

    @NotNull
    public static ParticleChunk of(@NotNull Chunk chunk, @NotNull World world, @NotNull HuskTowns plugin) {
        return new ParticleChunk(chunk, world, plugin);
    }

    @NotNull
    protected List<ParticleLine> getLines() {
        return List.of(
                ParticleLine.between(positions[0], positions[1]),
                ParticleLine.between(positions[1], positions[2]),
                ParticleLine.between(positions[2], positions[3]),
                ParticleLine.between(positions[3], positions[0])
        );
    }

}