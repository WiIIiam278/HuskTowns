package me.william278.husktowns.integration;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.object.chunk.ClaimedChunk;
import me.william278.husktowns.object.town.Town;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public class DynMap {

    private static Plugin dynMap;
    private static final HuskTowns plugin = HuskTowns.getInstance();

    /**
     * Returns the Marker Set used for Town Claims
     * @param dynMapAPI Instance of the Dynmap API
     * @return the MarkerSet used for displaying town claimed chunks
     */
    private static MarkerSet getMarkerSet(DynmapAPI dynMapAPI) {
        MarkerSet markerSet = dynMapAPI.getMarkerAPI().getMarkerSet("husktowns.towns");
        if (markerSet == null) {
            markerSet = dynMapAPI.getMarkerAPI().createMarkerSet("husktowns.towns", "Towns", dynMapAPI.getMarkerAPI().getMarkerIcons(), false);
        } else {
            markerSet.setMarkerSetLabel("Towns");
        }
        if (markerSet == null) {
            plugin.getLogger().warning("An exception occurred with the Dynmap integration; failed to create marker set.");
            return null;
        }
        return markerSet;
    }

    public static void removeAllClaimAreaMarkers() {
        DynmapAPI dynMapAPI = (DynmapAPI) dynMap;
        MarkerSet markerSet = getMarkerSet(dynMapAPI);
        if (markerSet != null) {
            for (AreaMarker marker : markerSet.getAreaMarkers()) {
                marker.deleteMarker();
            }
        }
    }

    public static void removeClaimAreaMarker(ClaimedChunk claimedChunk) {
        String markerId = "husktowns.claim." + claimedChunk.getTown() + "." + claimedChunk.getServer() + "." + claimedChunk.getWorld() + "." + claimedChunk.getChunkX() + "." + claimedChunk.getChunkZ();
        DynmapAPI dynMapAPI = (DynmapAPI) dynMap;

        MarkerSet markerSet = getMarkerSet(dynMapAPI);
        if (markerSet != null) {
            for (AreaMarker marker : markerSet.getAreaMarkers()) {
                if (marker.getMarkerID().equals(markerId)) {
                    marker.deleteMarker();
                }
            }
        }
    }

    public static String getTownPopup(ClaimedChunk claimedChunk) {
        String chunkTypeString = "";
        switch (claimedChunk.getChunkType()) {
            case FARM:
                chunkTypeString = "Farming Chunk Ⓕ";
                break;
            case REGULAR:
                chunkTypeString = "Town Claim";
                break;
            case PLOT:
                if (claimedChunk.getPlotChunkOwner() != null) {
                    chunkTypeString = HuskTowns.getPlayerCache().getUsername(claimedChunk.getPlotChunkOwner())  + "'s Plot Ⓟ";
                } else {
                    chunkTypeString = "Unclaimed Plot Ⓟ";
                }
        }
        if (claimedChunk.getTown().equals(HuskTowns.getSettings().getAdminTownName())) {
            chunkTypeString = "Admin Claim Ⓐ";
        }

        String townPopup = "<div class=\"infowindow\"><span style=\"font-weight:bold; color:%COLOR%;\">%TOWN_NAME%</span><br/><span style=\"font-style:italic;\">%CLAIM_TYPE%</span><br/><span style=\"font-weight:bold; color:%COLOR%\">Chunk: </span>%CHUNK%<br/><span style=\"font-weight:bold; color:%COLOR%\">Claimed: </span>%CLAIM_TIME%<br/><span style=\"font-weight:bold; color:%COLOR%\">By: </span>%CLAIMER%</div>";
        townPopup = townPopup.replace("%COLOR%", escapeHtml(Town.getTownColorHex(claimedChunk.getTown())));
        townPopup = townPopup.replace("%CLAIM_TYPE%", escapeHtml(chunkTypeString));
        townPopup = townPopup.replace("%TOWN_NAME%", escapeHtml(claimedChunk.getTown()));
        townPopup = townPopup.replace("%CHUNK%", escapeHtml(claimedChunk.getChunkX() + ", " + claimedChunk.getChunkZ()));
        townPopup = townPopup.replace("%CLAIM_TIME%", escapeHtml(claimedChunk.getFormattedTime()));
        if (HuskTowns.getPlayerCache().getUsername(claimedChunk.getClaimerUUID()) != null) {
            townPopup = townPopup.replace("%CLAIMER%", escapeHtml(HuskTowns.getPlayerCache().getUsername(claimedChunk.getClaimerUUID())));
        } else {
            townPopup = townPopup.replace("%CLAIMER%", "A citizen");
        }
        return townPopup;
    }

    public static void addClaimAreaMarker(ClaimedChunk claimedChunk) {
        removeClaimAreaMarker(claimedChunk);
        try {
            World world = Bukkit.getWorld(claimedChunk.getWorld());
            if (world == null) {
                return;
            }

            DynmapAPI dynMapAPI = (DynmapAPI) dynMap;
            MarkerSet markerSet = getMarkerSet(dynMapAPI);
            String markerId = "husktowns.claim." + claimedChunk.getTown() + "." + claimedChunk.getServer() + "." + claimedChunk.getWorld() + "." + claimedChunk.getChunkX() + "." + claimedChunk.getChunkZ();

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
            if (!HuskTowns.getSettings().useTownColorsOnDynMap()) {
                hexColor = HuskTowns.getSettings().getDefaultTownColor();
            }
            int color = Integer.parseInt(hexColor.substring(1), 16);
            final double fillOpacity = HuskTowns.getSettings().getDynmapFillOpacity();
            marker.setFillStyle(fillOpacity, color);

            // Set the stroke style (invisible)
            final double strokeOpacity = HuskTowns.getSettings().getDynmapStrokeOpacity();
            final int strokeWeight = HuskTowns.getSettings().getDynmapStrokeWeight();
            marker.setLineStyle(strokeWeight, strokeOpacity, color);

            marker.setLabel(claimedChunk.getTown());

            marker.setDescription(getTownPopup(claimedChunk));
        } catch (Exception e) {
            plugin.getLogger().warning("An exception occurred updating the Dynmap:" + e.getCause());
            e.printStackTrace();
        }
    }

    public static void initialize() {
        if (HuskTowns.getSettings().doDynMap()) {
            dynMap = plugin.getServer().getPluginManager().getPlugin("dynmap");
            if (dynMap == null) {
                HuskTowns.getSettings().setDoDynMap(false);
                plugin.getConfig().set("integrations.dynmap.enabled", false);
                plugin.saveConfig();
                return;
            }
            if (!dynMap.isEnabled()) {
                HuskTowns.getSettings().setDoDynMap(false);
                plugin.getConfig().set("integrations.dynmap.enabled", false);
                plugin.saveConfig();
                return;
            }
            plugin.getLogger().info("Enabled Dynmap integration!");
            getMarkerSet((DynmapAPI) dynMap);
        }
    }

}
