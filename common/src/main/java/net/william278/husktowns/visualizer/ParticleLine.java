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
