package net.william278.husktowns.hook;

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
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class Pl3xMapHook extends MapHook implements EventListener {

    private static final String CLAIMS_LAYER = "claim_markers";
    private final ConcurrentLinkedQueue<TownClaim> claims = new ConcurrentLinkedQueue<>();

    public Pl3xMapHook(@NotNull HuskTowns plugin) {
        super(plugin, "Pl3xMap");
    }

    @Override
    protected void onEnable() {
        Pl3xMap.api().getEventRegistry().register(this);
    }

    @Override
    public void setClaimMarker(@NotNull TownClaim claim, @NotNull World world) {
        claims.add(claim);
    }

    @Override
    public void removeClaimMarker(@NotNull TownClaim claim, @NotNull World world) {
        claims.remove(claim);
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
        return plugin.getSettings().getWebMapMarkerSetName();
    }

    @NotNull
    private String getClaimMarkerKey(@NotNull TownClaim claim, @NotNull net.pl3x.map.core.world.World world) {
        return plugin.getKey(
            claim.town().getName().toLowerCase(),
            Integer.toString(claim.claim().getChunk().getX()),
            Integer.toString(claim.claim().getChunk().getZ()),
            world.getName()
        ).toString();
    }

    private void registerLayers(@NotNull net.pl3x.map.core.world.World world) {
        if (plugin.getSettings().doWebMapHook()) {
            ClaimsLayer layer = new ClaimsLayer(this, world);
            world.getLayerRegistry().register(layer);
        }
    }

    @EventHandler
    public void onPl3xMapEnabled(@NotNull Pl3xMapEnabledEvent event) {
        // Register layers for each world
        Pl3xMap.api().getWorldRegistry().forEach(this::registerLayers);
    }

    @EventHandler
    public void onWorldLoaded(@NotNull WorldLoadedEvent event) {
        registerLayers(event.getWorld());
    }

    @EventHandler
    public void onWorldUnloaded(@NotNull WorldUnloadedEvent event) {
        event.getWorld().getLayerRegistry().unregister(CLAIMS_LAYER);
    }

    @NotNull
    public Options getMarkerOptions(@NotNull TownClaim claim) {
        return Options.builder()
            .tooltip(new Tooltip(claim.town().getName()))
            .fillColor(claim.town().getColor().getRGB())
            .strokeColor(claim.town().getColor().darker().getRGB())
            .strokeWeight(1)
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
            return hook.claims.stream()
                // TODO: Check if there is a way to filter claims by world
//                .filter(claim -> claim.claim().getChunk().getWorld().getName().equals(mapWorld.getName()))
                .map(claim -> Marker.rectangle(
                    hook.getClaimMarkerKey(claim, mapWorld),
                    Point.of((claim.claim().getChunk().getX() * 16), (claim.claim().getChunk().getZ() * 16)),
                    Point.of(((claim.claim().getChunk().getX() * 16) + 16), ((claim.claim().getChunk().getZ() * 16) + 16))
                ).setOptions(hook.getMarkerOptions(claim)))
                .collect(Collectors.toCollection(LinkedList::new));
        }
    }
}
