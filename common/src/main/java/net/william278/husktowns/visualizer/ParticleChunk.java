package net.william278.husktowns.visualizer;

import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ParticleChunk {

    // Variables for determining how to space particles for visualizing claims
    private static final double CHUNK_SIZE = 16d;
    private static final double PARTICLE_OFFSET = 0.025d;
    private static final double LINE_LENGTH = CHUNK_SIZE - PARTICLE_OFFSET;

    private final Position[] positions = new Position[4];

    private ParticleChunk(@NotNull Chunk chunk, @NotNull World world) {
        super();
        // North West
        positions[0] = Position.at((chunk.getX() * 16) + PARTICLE_OFFSET, 64,
                (chunk.getZ() * 16) + PARTICLE_OFFSET, world);

        // North East
        positions[1] = Position.at((chunk.getX() * 16) + PARTICLE_OFFSET, 64,
                (chunk.getZ() * 16) + LINE_LENGTH, world);

        // South East
        positions[2] = Position.at((chunk.getX() * 16) + LINE_LENGTH, 64,
                (chunk.getZ() * 16) + LINE_LENGTH, world);

        // South West
        positions[3] = Position.at((chunk.getX() * 16) + LINE_LENGTH, 64,
                (chunk.getZ() * 16) + PARTICLE_OFFSET, world);
    }

    @NotNull
    public static ParticleChunk of(@NotNull Chunk chunk, @NotNull World world) {
        return new ParticleChunk(chunk, world);
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