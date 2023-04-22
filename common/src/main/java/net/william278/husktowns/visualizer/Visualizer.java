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

package net.william278.husktowns.visualizer;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Visualizes a claim by outlining it with particles
 */
public class Visualizer {

    private final HuskTowns plugin;
    private final OnlineUser user;
    private final ParticleChunk chunk;
    private final Color color;
    private Integer taskId = null;
    private boolean done = false;

    public Visualizer(@NotNull OnlineUser user, @NotNull TownClaim claim, @NotNull World world, @NotNull HuskTowns plugin) {
        this.user = user;
        this.chunk = ParticleChunk.of(claim.claim().getChunk(), world);
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
                    .map(line -> line.getInterpolatedPositions(plugin))
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
