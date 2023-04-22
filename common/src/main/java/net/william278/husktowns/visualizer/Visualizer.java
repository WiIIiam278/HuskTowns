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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Visualizes a claim by outlining it with particles
 */
public class Visualizer {
    private static final int PARTICLE_COUNT = 3;
    private static final long PARTICLE_FREQUENCY = 10L;

    private final HuskTowns plugin;
    private final OnlineUser user;
    private final Map<Color, List<ParticleChunk>> chunks;
    private Integer taskId = null;
    private boolean done = false;

    public Visualizer(@NotNull OnlineUser user, @NotNull List<TownClaim> claims, @NotNull World world, @NotNull HuskTowns plugin) {
        this.user = user;
        this.plugin = plugin;
        this.chunks = new HashMap<>();
        for (TownClaim claim : claims) {
            this.chunks.computeIfAbsent(claim.town().getColor(), k -> new ArrayList<>())
                    .add(ParticleChunk.of(claim.claim().getChunk(), world));
        }
    }

    public void show(long duration) {
        if (done) {
            return;
        }
        final AtomicLong currentTicks = new AtomicLong();
        this.taskId = plugin.runTimedAsync(() -> {
            if (currentTicks.addAndGet(PARTICLE_FREQUENCY) > duration) {
                cancel();
                return;
            }
            this.chunks.forEach((color, chunks) -> chunks.forEach(chunk -> chunk.getLines().stream()
                    .map(line -> line.getInterpolatedPositions(plugin))
                    .forEach(line -> line.forEach(point -> user.spawnMarkerParticle(point, color, PARTICLE_COUNT)))));
        }, 0, PARTICLE_FREQUENCY);
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
