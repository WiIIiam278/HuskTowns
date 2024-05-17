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

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.hook.MapHook;
import net.william278.husktowns.hook.PluginHook;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class DynmapHook extends MapHook {

    @Nullable
    private DynmapCommonAPI dynmapApi;
    @Nullable
    private MarkerSet markerSet;
    @PluginHook(id = "Dynmap", register = PluginHook.Register.ON_ENABLE, platform = "common")
    public DynmapHook(@NotNull HuskTowns plugin) {
        super(plugin);
        DynmapCommonAPIListener.register(new DynmapCommonAPIListener() {
            @Override
            public void apiEnabled(@NotNull DynmapCommonAPI dynmapCommonAPI) {
                dynmapApi = dynmapCommonAPI;
                if (plugin.isLoaded()) {
                    onEnable();
                }
            }
        });
    }

    @Override
    public void onEnable() {
        getDynmap().ifPresent(api -> {
            clearAllMarkers();
            getMarkerSet();

            plugin.log(Level.INFO, "Enabled Dynmap markers hook. Populating web map with claims...");
            for (World world : plugin.getWorlds()) {
                plugin.getClaimWorld(world).ifPresent(claimWorld -> setClaimMarkers(claimWorld.getClaims(plugin), world));
            }
        });
    }

    private void addMarker(@NotNull TownClaim claim, @NotNull World world, @NotNull MarkerSet markerSet) {
        final Chunk chunk = claim.claim().getChunk();

        final String markerId = getClaimMarkerKey(claim, world);
        AreaMarker marker = markerSet.findAreaMarker(markerId);
        if (marker == null) {
            // Get the corner coordinates
            double[] x = new double[4];
            double[] z = new double[4];
            x[0] = chunk.getX() * 16;
            z[0] = chunk.getZ() * 16;
            x[1] = (chunk.getX() * 16) + 16;
            z[1] = (chunk.getZ() * 16);
            x[2] = (chunk.getX() * 16) + 16;
            z[2] = (chunk.getZ() * 16) + 16;
            x[3] = (chunk.getX() * 16);
            z[3] = (chunk.getZ() * 16) + 16;

            // Define the marker
            marker = markerSet.createAreaMarker(
                    markerId,
                    claim.town().getName(),
                    false,
                    world.getName(), x, z,
                    false
            );
        }

        // Set the marker y level
        final double markerY = 64;
        marker.setRangeY(markerY, markerY);

        // Set the fill and stroke colors
        final int color = Integer.parseInt(claim.town().getColorRgb().substring(1), 16);
        marker.setFillStyle(0.5f, color);
        marker.setLineStyle(1, 1, color);
        marker.setLabel(claim.town().getName());
    }

    private void removeMarker(@NotNull TownClaim claim, @NotNull World world) {
        getMarkerSet().ifPresent(markerSet -> markerSet.findAreaMarker(getClaimMarkerKey(claim, world)).deleteMarker());
    }


    @Override
    public void setClaimMarker(@NotNull TownClaim claim, @NotNull World world) {
        plugin.runSync(() -> getMarkerSet().ifPresent(markerSet -> addMarker(claim, world, markerSet)));
    }

    @Override
    public void removeClaimMarker(@NotNull TownClaim claim, @NotNull World world) {
        plugin.runSync(() -> getMarkerSet().ifPresent(markerSet -> removeMarker(claim, world)));
    }

    @Override
    public void setClaimMarkers(@NotNull List<TownClaim> townClaims, @NotNull World world) {
        plugin.runSync(() -> getMarkerSet().ifPresent(markerSet -> {
            for (TownClaim claim : townClaims) {
                addMarker(claim, world, markerSet);
            }
        }));
    }

    @Override
    public void removeClaimMarkers(@NotNull List<TownClaim> townClaims, @NotNull World world) {
        plugin.runSync(() -> getMarkerSet().ifPresent(markerSet -> {
            for (TownClaim claim : townClaims) {
                removeMarker(claim, world);
            }
        }));
    }

    @Override
    public void clearAllMarkers() {
        plugin.runSync(() -> getMarkerSet().ifPresent(markerSet -> markerSet.getAreaMarkers()
                .forEach(AreaMarker::deleteMarker)));
    }

    @NotNull
    private String getClaimMarkerKey(@NotNull TownClaim claim, @NotNull World world) {
        return plugin.getKey(
                Integer.toString(claim.town().getId()),
                Integer.toString(claim.claim().getChunk().getX()),
                Integer.toString(claim.claim().getChunk().getZ()),
                world.getName()
        ).toString();
    }

    private Optional<DynmapCommonAPI> getDynmap() {
        return Optional.ofNullable(dynmapApi);
    }

    private Optional<MarkerSet> getMarkerSet() {
        return getDynmap().map(api -> {
            final String setLabel = plugin.getSettings().getGeneral().getWebMapHook().getMarkerSetName();
            markerSet = api.getMarkerAPI().getMarkerSet(getMarkerSetKey());
            if (markerSet == null) {
                markerSet = api.getMarkerAPI().createMarkerSet(
                        getMarkerSetKey(),
                        setLabel,
                        api.getMarkerAPI().getMarkerIcons(),
                        false
                );
            } else {
                markerSet.setMarkerSetLabel(setLabel);
            }
            return markerSet;
        });
    }

}
