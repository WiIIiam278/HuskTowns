package net.william278.husktowns.hook.map;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.chunk.ClaimedChunk;
import net.william278.husktowns.town.Town;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import xyz.jpenilla.squaremap.api.Point;
import xyz.jpenilla.squaremap.api.*;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;
import xyz.jpenilla.squaremap.api.marker.Rectangle;

import java.awt.*;
import java.util.HashMap;
import java.util.Set;

public class SquareMap extends Map {

    private final HashMap<WorldIdentifier, SimpleLayerProvider> providers = new HashMap<>();

    @Override
    public void initialize() {
        Plugin pl3xMap = Bukkit.getPluginManager().getPlugin("Squaremap");
        if (pl3xMap == null) {
            HuskTowns.getSettings().doMapIntegration = false;
            return;
        }
        if (!pl3xMap.isEnabled()) {
            HuskTowns.getSettings().doMapIntegration = false;
            return;
        }
        Squaremap mapAPI = SquaremapProvider.get();
        for (MapWorld world : mapAPI.mapWorlds()) {
            SimpleLayerProvider provider = SimpleLayerProvider
                    .builder(HuskTowns.getSettings().mapMarkerSetName)
                    .showControls(true)
                    .defaultHidden(false)
                    .layerPriority(5)
                    .zIndex(250)
                    .build();
            world.layerRegistry().register(Key.of(MARKER_SET_ID), provider);
            providers.put(world.identifier(), provider);
        }
    }

    private MarkerOptions getMarkerOptions(ClaimedChunk claimedChunk) {
        Color color;
        if (HuskTowns.getSettings().useTownColorsOnMap) {
            color = Town.getTownColor(claimedChunk.getTown());
        } else {
            color = Color.decode(HuskTowns.getSettings().mapTownColor);
        }
        return MarkerOptions.builder()
                .fill(true)
                .stroke(true)
                .hoverTooltip(claimedChunk.getTown())
                .clickTooltip(getClaimInfoWindow(claimedChunk))
                .strokeColor(color)
                .strokeOpacity(HuskTowns.getSettings().mapClaimStrokeOpacity)
                .strokeWeight(HuskTowns.getSettings().mapClaimStrokeWeight)
                .fillColor(color)
                .fillOpacity(HuskTowns.getSettings().mapClaimFillOpacity)
                .build();
    }

    @Override
    public void addMarker(ClaimedChunk claimedChunk) {
        World world = Bukkit.getWorld(claimedChunk.getWorld());
        if (world != null) {
            for (WorldIdentifier worldIdentifier : providers.keySet()) {
                if (worldIdentifier.value().equals(world.getName())) {
                    Point point1 = Point.of(claimedChunk.getChunkX() * 16, claimedChunk.getChunkZ() * 16);
                    Point point2 = Point.of((claimedChunk.getChunkX() * 16) + 16, (claimedChunk.getChunkZ() * 16) + 16);
                    Rectangle marker = Marker.rectangle(point1, point2);
                    marker.markerOptions(getMarkerOptions(claimedChunk));
                    providers.get(worldIdentifier).addMarker(Key.of(getClaimMarkerId(claimedChunk)), marker);
                    return;
                }
            }
        }
    }

    @Override
    public void removeMarker(ClaimedChunk claimedChunk) {
        World world = Bukkit.getWorld(claimedChunk.getWorld());
        if (world != null) {
            for (WorldIdentifier worldIdentifier : providers.keySet()) {
                if (worldIdentifier.value().equals(world.getName())) {
                    providers.get(worldIdentifier).removeMarker(Key.of(getClaimMarkerId(claimedChunk)));
                    return;
                }
            }
        }
    }

    @Override
    public void addMarkers(Set<ClaimedChunk> claimedChunks) {
        for (ClaimedChunk chunk : claimedChunks) {
            addMarker(chunk);
        }
    }

    @Override
    public void removeMarkers(Set<ClaimedChunk> claimedChunks) {
        for (ClaimedChunk chunk : claimedChunks) {
            removeMarker(chunk);
        }
    }

    @Override
    public void clearMarkers() {
        for (SimpleLayerProvider provider : providers.values()) {
            provider.getMarkers().clear();
        }
    }
}
