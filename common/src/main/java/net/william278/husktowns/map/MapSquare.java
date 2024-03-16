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

package net.william278.husktowns.map;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MapSquare {

    private final char claimChar;
    private final char wildernessChar;

    private final HuskTowns plugin;
    @Nullable
    private final TownClaim claim;
    private final Chunk chunk;
    private final World world;
    private boolean isCurrentPosition;

    private MapSquare(@NotNull Chunk chunk, @NotNull World world, @Nullable TownClaim claim, @NotNull HuskTowns plugin) {
        this.chunk = chunk;
        this.world = world;
        this.claim = claim;
        this.plugin = plugin;
        this.claimChar = plugin.getSettings().getGeneral().getClaimMapClaimChar();
        this.wildernessChar = plugin.getSettings().getGeneral().getClaimMapWildernessChar();
    }

    @NotNull
    public static MapSquare claim(@NotNull Chunk chunk, @NotNull World world, @Nullable TownClaim claim,
                                  @NotNull HuskTowns plugin) {
        return new MapSquare(chunk, world, claim, plugin);
    }

    @NotNull
    public static MapSquare wilderness(@NotNull Chunk chunk, @NotNull World world, @NotNull HuskTowns plugin) {
        return new MapSquare(chunk, world, null, plugin);
    }

    @NotNull
    private Component getSquareTooltip() {
        Component component = getSquareHeaderLocale()
                .map(MineDown::toComponent).orElse(Component.empty());
        if (!isWilderness() && (claim.claim().getType() != Claim.Type.CLAIM || claim.isAdminClaim(plugin))) {
            component = component.appendNewline().append(getSquareTypeLocale()
                    .map(MineDown::toComponent).orElse(Component.empty()));
        }
        component = component.appendNewline()
                .append(plugin.getLocales().getLocale("claim_map_square_coordinates",
                                Integer.toString(chunk.getX()), Integer.toString(chunk.getZ()))
                        .map(MineDown::toComponent).orElse(Component.empty()));
        if (isCurrentPosition) {
            component = component.appendNewline()
                    .append(plugin.getLocales().getLocale("claim_map_square_currently_here")
                            .map(MineDown::toComponent).orElse(Component.empty()));
        }
        return component;
    }

    private Optional<MineDown> getSquareTypeLocale() {
        if (claim == null) {
            return Optional.empty();
        }

        if (claim.isAdminClaim(plugin)) {
            return plugin.getLocales().getLocale("claim_map_square_admin");
        }

        return switch (claim.claim().getType()) {
            case FARM -> plugin.getLocales().getLocale("claim_map_square_farm");
            case PLOT -> plugin.getLocales().getLocale("claim_map_square_plot");
            default -> Optional.empty();
        };
    }

    private Optional<MineDown> getSquareHeaderLocale() {
        if (isProtected()) {
            return plugin.getLocales().getLocale("claim_map_square_unclaimable");
        }
        if (isUnclaimable()) {
            return plugin.getLocales().getLocale("claim_map_square_unclaimable");
        }
        if (isWilderness()) {
            return plugin.getLocales().getLocale("claim_map_square_wilderness");
        }
        return plugin.getLocales().getLocale("claim_map_square_town_name", claim.town().getName(),
                getSquareColor());
    }

    @NotNull
    private String getSquareColor() {
        if (isProtected() || isUnclaimable()) {
            return "#780000";
        }
        if (isWilderness()) {
            return "#2e2e2e";
        }
        return claim.town().getColorRgb();
    }

    @NotNull
    public Component toComponent() {
        Component component = Component.text(isWilderness() ? wildernessChar : claimChar)
                .color(TextColor.fromHexString(getSquareColor()))
                .hoverEvent(getSquareTooltip());
        if (!isUnclaimable()) {
            if (isWilderness()) {
                component = component.clickEvent(ClickEvent.runCommand("/town claim " + chunk.getX() + " " + chunk.getZ() + " -m"));
            } else if (!claim.isAdminClaim(plugin)) {
                component = component.clickEvent(ClickEvent.runCommand("/town info " + claim.town().getName()));
            }
        }
        return component;

    }

    private boolean isUnclaimable() {
        return !plugin.getClaimWorlds().containsKey(world.getName());
    }

    private boolean isWilderness() {
        return claim == null;
    }

    private boolean isProtected() {
        return plugin.getWorldGuardHook() != null && plugin.getWorldGuardHook().isChunkInRestrictedRegion(chunk, world.getName());
    }
    public void markAsCurrentPosition(boolean isCurrentPosition) {
        this.isCurrentPosition = isCurrentPosition;
    }
}
