package net.william278.husktowns.visualizer;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.concurrent.atomic.AtomicLong;

public class Visualizer {

    private final HuskTowns plugin;
    private final OnlineUser user;
    private final ParticleChunk chunk;
    private final Color color;
    private Integer taskId = null;
    private boolean done = false;

    public Visualizer(@NotNull OnlineUser user, @NotNull TownClaim claim, @NotNull World world, @NotNull HuskTowns plugin) {
        this.user = user;
        this.chunk = ParticleChunk.of(claim.claim().getChunk(), world, plugin);
        this.color = claim.town().getColor();
        this.plugin = plugin;
    }

    public void show(long duration) {
        if (done) {
            return;
        }
        final long PERIOD = 10L;
        final AtomicLong currentTicks = new AtomicLong();
        this.taskId = plugin.runTimedAsync(() -> {
            if (currentTicks.addAndGet(PERIOD) > duration) {
                cancel();
                return;
            }
            chunk.getLines().stream()
                    .map(ParticleLine::getInterpolatedPositions)
                    .forEach(line -> line.forEach(point -> user.spawnMarkerParticle(point, color, 3)));
        }, 0, PERIOD);
    }

    public void cancel() {
        if (done) {
            return;
        }
        if (taskId != null) {
            plugin.cancelTask(taskId);
        }
        this.done = true;
    }

}
