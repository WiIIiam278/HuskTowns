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

package net.william278.husktowns.hook.map;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.hook.MapHook;
import net.william278.husktowns.hook.PluginHook;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public final class BlueMapHook extends MapHook {

    private Map<String, MarkerSet> markerSets;
    @PluginHook(id = "BlueMap", register = PluginHook.Register.ON_ENABLE, platform = "common")
    public BlueMapHook(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        BlueMapAPI.onEnable(api -> {
            clearAllMarkers();

            this.markerSets = new HashMap<>();
            for (World world : plugin.getWorlds()) {
                getMapWorld(world).ifPresent(mapWorld -> {
                    final MarkerSet markerSet = MarkerSet.builder()
                            .label(plugin.getSettings().getGeneral().getWebMapHook().getMarkerSetName())
                            .build();
                    for (BlueMapMap map : mapWorld.getMaps()) {
                        map.getMarkerSets().put(plugin.getKey(map.getId()).toString(), markerSet);
                    }
                    markerSets.put(world.getName(), markerSet);
                });
            }

            plugin.log(Level.INFO, "Enabled BlueMap markers hook. Populating web map with claims...");
            for (World world : plugin.getWorlds()) {
                plugin.getClaimWorld(world).ifPresent(claimWorld -> setClaimMarkers(claimWorld.getClaims(plugin), world));
            }
        });
    }

    @NotNull
    private ShapeMarker getClaimMarker(@NotNull TownClaim claim) {
        final int x = claim.claim().getChunk().getX() * 16;
        final int z = claim.claim().getChunk().getZ() * 16;
        return ShapeMarker.builder()
                .label(claim.town().getName())
                .fillColor(new Color(
                        claim.town().getDisplayColor().red(),
                        claim.town().getDisplayColor().green(),
                        claim.town().getDisplayColor().blue(),
                        0.5f
                ))
                .lineColor(new Color(
                        claim.town().getDisplayColor().red(),
                        claim.town().getDisplayColor().green(),
                        claim.town().getDisplayColor().blue(),
                        1f
                ))
                .shape(Shape.createRect(x, z, x + 16, z + 16), 64)
                .lineWidth(1)
                .depthTestEnabled(false)
                .build();
    }

    @NotNull
    private String getClaimMarkerKey(@NotNull TownClaim claim) {
        return plugin.getKey(
                Integer.toString(claim.town().getId()),
                Integer.toString(claim.claim().getChunk().getX()),
                Integer.toString(claim.claim().getChunk().getZ())
        ).toString();
    }

    @Override
    public void setClaimMarker(@NotNull TownClaim claim, @NotNull World world) {
        getMarkerSet(world).ifPresent(markerSet -> markerSet.put(getClaimMarkerKey(claim), getClaimMarker(claim)));
    }

    @Override
    public void setClaimMarkers(@NotNull List<TownClaim> claims, @NotNull World world) {
        getMarkerSet(world).ifPresent(markerSet -> plugin.getClaimWorld(world).ifPresent(claimWorld -> {
            for (TownClaim claim : claims) {
                markerSet.put(getClaimMarkerKey(claim), getClaimMarker(claim));
            }
        }));
    }

    @Override
    public void removeClaimMarker(@NotNull TownClaim claim, @NotNull World world) {
        getMarkerSet(world).ifPresent(markerSet -> markerSet.remove(getClaimMarkerKey(claim)));
    }

    @Override
    public void removeClaimMarkers(@NotNull List<TownClaim> claims, @NotNull World world) {
        getMarkerSet(world).ifPresent(markerSet -> plugin.getClaimWorld(world).ifPresent(claimWorld -> {
            for (TownClaim claim : claims) {
                markerSet.remove(getClaimMarkerKey(claim));
            }
        }));
    }

    @Override
    public void clearAllMarkers() {
        if (markerSets != null) {
            for (MarkerSet markerSet : markerSets.values()) {
                for (String markerId : markerSet.getMarkers().keySet()) {
                    markerSet.remove(markerId);
                }
            }
        }
    }

    @NotNull
    private Optional<MarkerSet> getMarkerSet(@NotNull World world) {
        return markerSets == null ? Optional.empty() : Optional.ofNullable(markerSets.get(world.getName()));
    }

    @NotNull
    private Optional<BlueMapWorld> getMapWorld(@NotNull World world) {
        return BlueMapAPI.getInstance().flatMap(api -> api.getWorld(world.getName()));
    }

}
