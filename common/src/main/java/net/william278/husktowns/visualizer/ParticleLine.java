package net.william278.husktowns.visualizer;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Position;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ParticleLine {

    private static final double PARTICLE_SPACING = 0.2d;
    public Position start;
    public Position end;

    private ParticleLine(@NotNull Position start, @NotNull Position end) {
        this.start = start;
        this.end = end;
    }

    @NotNull
    public static ParticleLine between(@NotNull Position start, @NotNull Position end) {
        return new ParticleLine(start, end);
    }

    @NotNull
    protected List<Position> getInterpolatedPositions(@NotNull HuskTowns plugin) {
        final List<Position> positions = new ArrayList<>();
        final double distance = start.distanceBetween(end);
        final double step = PARTICLE_SPACING / distance;
        for (double t = 0; t < 1; t += step) {
            positions.add(start.interpolate(end, t));
        }
        positions.add(end);
        positions.forEach(position -> position.setY(plugin.getHighestBlockAt(position)));
        return positions;
    }

}
