package me.william278.husktowns.integrations.map;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.chunk.ClaimedChunk;
import me.william278.husktowns.town.Town;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;

import java.util.HashSet;
import java.util.Set;

public class DynMap extends Map {

    private DynmapAPI getDynMapAPI() {
        return (DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap");
    }
    private static final HuskTowns plugin = HuskTowns.getInstance();

    // Returns the marker set used for town claims
    private MarkerSet getMarkerSet() {
        DynmapAPI dynmapAPI = getDynMapAPI();
        MarkerSet markerSet = dynmapAPI.getMarkerAPI().getMarkerSet(MARKER_SET_ID);
        if (markerSet == null) {
            markerSet = dynmapAPI.getMarkerAPI().createMarkerSet(MARKER_SET_ID, HuskTowns.getSettings().getMapMarkerSetName(), dynmapAPI.getMarkerAPI().getMarkerIcons(), false);
        } else {
            markerSet.setMarkerSetLabel(HuskTowns.getSettings().getMapMarkerSetName());
        }
        if (markerSet == null) {
            plugin.getLogger().warning("An exception occurred with the Dynmap integration; failed to create marker set.");
            return null;
        }
        return markerSet;
    }

    @Override
    public void initialize() {
        Plugin dynMap = Bukkit.getPluginManager().getPlugin("dynmap");
        if (dynMap == null) {
            HuskTowns.getSettings().setDoMapIntegration(false);
            return;
        }
        if (!dynMap.isEnabled()) {
            HuskTowns.getSettings().setDoMapIntegration(false);
            return;
        }
        getMarkerSet();
    }

    @Override
    public void addMarker(ClaimedChunk claimedChunk) {
        removeMarker(claimedChunk);
        try {
            World world = Bukkit.getWorld(claimedChunk.getWorld());
            if (world == null) {
                return;
            }

            MarkerSet markerSet = getMarkerSet();
            String markerId = getClaimMarkerId(claimedChunk);

            if (markerSet == null) {
                plugin.getLogger().warning("An exception occurred adding a claim to the Dynmap; failed to retrieve marker set.");
                return;
            }

            // Get the corner coordinates
            double[] x = new double[4];
            double[] z = new double[4];

            x[0] = claimedChunk.getChunkX() * 16; z[0] = claimedChunk.getChunkZ() * 16;
            x[1] = (claimedChunk.getChunkX() * 16) + 16; z[1] = (claimedChunk.getChunkZ() * 16);
            x[2] = (claimedChunk.getChunkX() * 16) + 16; z[2] = (claimedChunk.getChunkZ() * 16) + 16;
            x[3] = (claimedChunk.getChunkX() * 16); z[3] = (claimedChunk.getChunkZ() * 16) + 16;

            // Define the marker
            AreaMarker marker = markerSet.createAreaMarker(markerId, claimedChunk.getTown(), false,
                    claimedChunk.getWorld(), x, z, false);
            double markerYRender = world.getSeaLevel();
            marker.setRangeY(markerYRender, markerYRender);

            // Set the fill style
            String hexColor = Town.getTownColorHex(claimedChunk.getTown());
            if (!HuskTowns.getSettings().useTownColorsOnMap()) {
                hexColor = HuskTowns.getSettings().getMapTownColor();
            }
            int color = Integer.parseInt(hexColor.substring(1), 16);
            final double fillOpacity = HuskTowns.getSettings().getMapFillOpacity();
            marker.setFillStyle(fillOpacity, color);

            // Set the stroke style (invisible)
            final double strokeOpacity = HuskTowns.getSettings().getMapStrokeOpacity();
            final int strokeWeight = HuskTowns.getSettings().getMapStrokeWeight();

            // Apply the values to the marker
            marker.setLineStyle(strokeWeight, strokeOpacity, color);
            marker.setLabel(claimedChunk.getTown());
            marker.setDescription(getClaimInfoWindow(claimedChunk));
        } catch (Exception e) {
            plugin.getLogger().warning("An exception occurred updating the Dynmap:" + e.getCause());
            e.printStackTrace();
        }
    }

    @Override
    public void removeMarker(ClaimedChunk claimedChunk) {
        String markerId = getClaimMarkerId(claimedChunk);

        MarkerSet markerSet = getMarkerSet();
        if (markerSet != null) {
            for (AreaMarker marker : markerSet.getAreaMarkers()) {
                if (marker.getMarkerID().equals(markerId)) {
                    marker.deleteMarker();
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
        HashSet<String> markersToRemove = new HashSet<>();
        for (ClaimedChunk chunk : claimedChunks) {
            markersToRemove.add(getClaimMarkerId(chunk));
        }
        MarkerSet markerSet = getMarkerSet();
        if (markerSet != null) {
            for (AreaMarker marker : markerSet.getAreaMarkers()) {
                if (markersToRemove.contains(marker.getMarkerID())) {
                    marker.deleteMarker();
                    markersToRemove.remove(marker.getMarkerID());
                }
            }
        }
    }

    @Override
    public void clearMarkers() {
        MarkerSet markerSet = getMarkerSet();
        if (markerSet != null) {
            for (AreaMarker marker : markerSet.getAreaMarkers()) {
                marker.deleteMarker();
            }
        }
    }
}
