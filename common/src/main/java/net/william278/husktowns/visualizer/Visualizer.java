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

package net.william278.husktowns.visualizer;

import net.kyori.adventure.text.format.TextColor;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.util.Task;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Visualizes a claim by outlining it with particles
 */
public class Visualizer {
    private static final int PARTICLE_COUNT = 3;
    private static final long PARTICLE_FREQUENCY = 10L;

    private final HuskTowns plugin;
    private final OnlineUser user;
    private final Map<TextColor, List<ParticleChunk>> chunks;
    private Task.Repeating task = null;
    private boolean done = false;

    public Visualizer(@NotNull OnlineUser user, @NotNull List<TownClaim> claims, @NotNull World world, @NotNull HuskTowns plugin) {
        this.user = user;
        this.plugin = plugin;
        this.chunks = new ConcurrentHashMap<>();
        for (TownClaim claim : claims) {
            this.chunks.computeIfAbsent(claim.town().getDisplayColor(), k -> new ArrayList<>())
                .add(ParticleChunk.of(claim.claim().getChunk(), world));
        }
    }

    public void show(long duration) {
        if (done) {
            return;
        }
        final AtomicLong currentTicks = new AtomicLong();
        this.task = plugin.getRepeatingTask(() -> {
            if (currentTicks.addAndGet(PARTICLE_FREQUENCY) > duration) {
                cancel();
                return;
            }
            this.chunks.forEach((color, chunks) -> chunks.forEach(chunk -> chunk.getLines().stream()
                .map(line -> line.getInterpolatedPositions(plugin))
                .forEach(line -> line.forEach(point -> user.spawnMarkerParticle(point, color, PARTICLE_COUNT)))));
        }, PARTICLE_FREQUENCY);
        this.task.run();
    }

    public void cancel() {
        if (done) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        this.done = true;
    }

}
