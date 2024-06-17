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

import net.kyori.adventure.text.format.TextColor;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.event.EventHandler;
import net.pl3x.map.core.event.EventListener;
import net.pl3x.map.core.event.server.Pl3xMapEnabledEvent;
import net.pl3x.map.core.event.world.WorldLoadedEvent;
import net.pl3x.map.core.event.world.WorldUnloadedEvent;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.layer.SimpleLayer;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.markers.option.Tooltip;
import net.pl3x.map.core.util.Colors;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.hook.MapHook;
import net.william278.husktowns.hook.PluginHook;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

public class Pl3xMapHook extends MapHook {

    private static final String CLAIMS_LAYER = "claim_markers";
    private final ConcurrentHashMap<ClaimWorld, ConcurrentLinkedQueue<TownClaim>> claims = new ConcurrentHashMap<>();

    @PluginHook(id = "Pl3xMap", register = PluginHook.Register.ON_ENABLE, platform = "common")
    public Pl3xMapHook(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        Pl3xMap.api().getEventRegistry().register(new Pl3xEvents(this));

        if (Pl3xMap.api().isEnabled()) {
            Pl3xMap.api().getWorldRegistry().forEach(this::registerLayers);
        }

        plugin.log(Level.INFO, "Enabled Pl3xMap markers hook. Populating web map with claims...");
        for (World world : plugin.getWorlds()) {
            plugin.getClaimWorld(world).ifPresent(claimWorld -> setClaimMarkers(claimWorld.getClaims(plugin), world));
        }
    }

    @Override
    public void setClaimMarker(@NotNull TownClaim claim, @NotNull World world) {
        plugin.getClaimWorld(world).ifPresent(claimWorld -> {
            ConcurrentLinkedQueue<TownClaim> townClaims = claims.computeIfAbsent(claimWorld, k -> new ConcurrentLinkedQueue<>());
            townClaims.add(claim);
        });
    }

    @Override
    public void removeClaimMarker(@NotNull TownClaim claim, @NotNull World world) {
        plugin.getClaimWorld(world).ifPresent(claimWorld -> {
            ConcurrentLinkedQueue<TownClaim> townClaims = claims.get(claimWorld);

            if (townClaims != null) {
                townClaims.remove(claim);
            }
        });
    }

    @Override
    public void setClaimMarkers(@NotNull List<TownClaim> claims, @NotNull World world) {
        claims.forEach(claim -> setClaimMarker(claim, world));
    }

    @Override
    public void removeClaimMarkers(@NotNull List<TownClaim> claims, @NotNull World world) {
        claims.forEach(claim -> removeClaimMarker(claim, world));
    }

    @Override
    public void clearAllMarkers() {
        claims.clear();
    }

    @NotNull
    private String getLayerName() {
        return plugin.getSettings().getGeneral().getWebMapHook().getMarkerSetName();
    }

    @NotNull
    private String getClaimMarkerKey(@NotNull TownClaim claim, @NotNull net.pl3x.map.core.world.World world) {
        return plugin.getKey(
            Integer.toString(claim.town().getId()),
            Integer.toString(claim.claim().getChunk().getX()),
            Integer.toString(claim.claim().getChunk().getZ()),
            world.getName()
        ).toString();
    }

    private void registerLayers(@NotNull net.pl3x.map.core.world.World mapWorld) {
        ClaimsLayer layer = new ClaimsLayer(this, mapWorld);
        mapWorld.getLayerRegistry().register(layer);
    }

    @NotNull
    public Options getMarkerOptions(@NotNull TownClaim claim) {
        final TextColor color = claim.town().getDisplayColor();
        return Options.builder()
            .tooltip(new Tooltip(claim.town().getName()).setDirection(Tooltip.Direction.TOP))
            .fillColor(Colors.argb(255 / 2, color.red(), color.green(), color.blue()))
            .strokeColor(Colors.rgb((int) (color.red() * 0.7), (int) (color.green() * 0.7), (int) (color.blue() * 0.7)))
            .build();
    }

    public static class ClaimsLayer extends SimpleLayer {

        private final Pl3xMapHook hook;
        private final net.pl3x.map.core.world.World mapWorld;

        public ClaimsLayer(@NotNull Pl3xMapHook hook, @NotNull net.pl3x.map.core.world.World mapWorld) {
            super(CLAIMS_LAYER, hook::getLayerName);
            this.hook = hook;
            this.mapWorld = mapWorld;
        }

        @Override
        @NotNull
        public Collection<Marker<?>> getMarkers() {
            Collection<Marker<?>> markers = new ArrayList<>();

            Optional<ClaimWorld> world = Optional.ofNullable(hook.plugin.getClaimWorlds().get(mapWorld.getName()));

            world.ifPresent(claimWorld -> {
                ConcurrentLinkedQueue<TownClaim> claimQueue = hook.claims.get(claimWorld);

                if (claimQueue != null) {
                    claimQueue.forEach(claim -> markers.add(Marker.rectangle(
                        hook.getClaimMarkerKey(claim, mapWorld),
                        Point.of((claim.claim().getChunk().getX() * 16), (claim.claim().getChunk().getZ() * 16)),
                        Point.of(((claim.claim().getChunk().getX() * 16) + 16), ((claim.claim().getChunk().getZ() * 16) + 16))
                    ).setOptions(hook.getMarkerOptions(claim))));
                }
            });

            return markers;
        }
    }

    @SuppressWarnings("unused")
    public static class Pl3xEvents implements EventListener {

        private final Pl3xMapHook hook;

        public Pl3xEvents(@NotNull Pl3xMapHook hook) {
            this.hook = hook;
        }

        @EventHandler
        public void onPl3xMapEnabled(@NotNull Pl3xMapEnabledEvent event) {
            // Register layers for each world
            Pl3xMap.api().getWorldRegistry().forEach(hook::registerLayers);
        }

        @EventHandler
        public void onWorldLoaded(@NotNull WorldLoadedEvent event) {
            hook.registerLayers(event.getWorld());
        }

        @EventHandler
        public void onWorldUnloaded(@NotNull WorldUnloadedEvent event) {
            event.getWorld().getLayerRegistry().unregister(CLAIMS_LAYER);
        }
    }
}
