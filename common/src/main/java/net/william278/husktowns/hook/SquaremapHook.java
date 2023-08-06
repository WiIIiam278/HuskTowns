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

package net.william278.husktowns.hook;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.jpenilla.squaremap.api.*;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;

import java.util.*;
import java.util.logging.Level;

public class SquaremapHook extends MapHook {
    @Nullable
    private Squaremap squaremapApi;
    private final Key layerKey = Key.of("husktowns");

    public SquaremapHook(@NotNull HuskTowns plugin) {
        super(plugin, "squaremap");
        try {
            squaremapApi = SquaremapProvider.get();
        } catch(IllegalStateException e) {
            plugin.log(Level.WARNING, "Failed to register squaremap extension", e);
        }
    }

    @Override
    protected void onEnable() {
        getSquaremap().ifPresent(api -> {
            clearAllMarkers();

            plugin.log(Level.INFO, "Enabled squaremap markers hook. Populating web map with claims...");
            for (World world : plugin.getWorlds()) {
                plugin.getClaimWorld(world).ifPresent(claimWorld -> setClaimMarkers(claimWorld.getClaims(plugin), world));
            }
        });
    }

    private void addMarker(@NotNull TownClaim claim, @NotNull World world) {
        final Chunk chunk = claim.claim().getChunk();
        final Key markerKey = getClaimMarkerKey(claim, world);

        getOrRegisterLayerProvider(world).ifPresent(layerProvider -> {
            Marker marker;
            if (layerProvider.hasMarker(markerKey)) {
                marker = layerProvider.registeredMarkers().get(markerKey);
            }
            else {
                Point point1 = Point.of((chunk.getX() * 16), (chunk.getZ() * 16));
                Point point2 = Point.of(((chunk.getX() * 16) + 16), ((chunk.getZ() * 16) + 16));
                marker = Marker.rectangle(point1, point2);

                layerProvider.addMarker(markerKey, marker);
            }

            MarkerOptions markerOptions = MarkerOptions.builder()
                .fillColor(claim.town().getColor())
                .strokeColor(claim.town().getColor().darker())
                .strokeWeight(1)
                .hoverTooltip("&l**Town:**" + claim.town().getName())
                .build();
            marker.markerOptions(markerOptions);
        });
    }

    private void removeMarker(@NotNull TownClaim claim, @NotNull World world) {
        getOrRegisterLayerProvider(world).ifPresent(layerProvider -> layerProvider.removeMarker(getClaimMarkerKey(claim, world)));
    }

    @Override
    public void setClaimMarker(@NotNull TownClaim claim, @NotNull World world) {
        addMarker(claim, world);
    }

    @Override
    public void removeClaimMarker(@NotNull TownClaim claim, @NotNull World world) {
        removeMarker(claim, world);
    }

    @Override
    public void setClaimMarkers(@NotNull List<TownClaim> claims, @NotNull World world) {
        claims.forEach(claim -> addMarker(claim, world));
    }

    @Override
    public void removeClaimMarkers(@NotNull List<TownClaim> claims, @NotNull World world) {
        claims.forEach(claim -> removeMarker(claim, world));
    }

    @Override
    public void clearAllMarkers() {
        plugin.getWorlds().forEach(world -> getOrRegisterLayerProvider(world).ifPresent(SimpleLayerProvider::clearMarkers));
    }

    @NotNull
    private Key getClaimMarkerKey(@NotNull TownClaim claim, @NotNull World world) {
        return Key.of(plugin.getKey(
            claim.town().getName().toLowerCase(),
            Integer.toString(claim.claim().getChunk().getX()),
            Integer.toString(claim.claim().getChunk().getZ()),
            world.getName()
        ).toString().replace("/", "_").replace(":", "_"));
    }

    private Optional<Squaremap> getSquaremap() {
        return Optional.ofNullable(squaremapApi);
    }

    @NotNull
    private Optional<SimpleLayerProvider> getOrRegisterLayerProvider(@NotNull World world) {
        return getMapWorld(world).map(mapWorld -> {
            if (mapWorld.layerRegistry().hasEntry(layerKey)) {
                return (SimpleLayerProvider) mapWorld.layerRegistry().get(layerKey);
            }
            else {
                SimpleLayerProvider layerProvider = SimpleLayerProvider.builder(plugin.getSettings().getWebMapMarkerSetName())
                    .showControls(true)
                    .defaultHidden(false)
                    .layerPriority(10)
                    .zIndex(250)
                    .build();
                mapWorld.layerRegistry().register(layerKey, layerProvider);
                return layerProvider;
            }
        });
    }

    @NotNull
    private Optional<MapWorld> getMapWorld(@NotNull World world) {
        return getSquaremap().map(api -> {
            // TODO: Find out proper way of implementing this with HuskTowns World type
            WorldIdentifier worldIdentifier = WorldIdentifier.create("minecraft", "overworld");
            return api.getWorldIfEnabled(worldIdentifier).orElse(null);
        });
    }
}
