package net.william278.husktowns.integrations.map;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.chunk.ClaimedChunk;
import net.william278.husktowns.town.Town;

import java.util.Set;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public abstract class Map {
    public Map() { }

    public static final String MARKER_SET_ID = "husktowns.towns";

    public abstract void initialize();

    public abstract void addMarker(ClaimedChunk claimedChunk);
    public abstract void removeMarker(ClaimedChunk claimedChunk);
    public abstract void addMarkers(Set<ClaimedChunk> claimedChunks);
    public abstract void removeMarkers(Set<ClaimedChunk> claimedChunks);
    public abstract void clearMarkers();

    public String getClaimInfoWindow(ClaimedChunk claimedChunk) {
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
                    chunkTypeString = HuskTowns.getPlayerCache().getPlayerUsername(claimedChunk.getPlotChunkOwner())  + "'s Plot Ⓟ";
                } else {
                    chunkTypeString = "Unclaimed Plot Ⓟ";
                }
        }
        if (claimedChunk.getTown().equals(HuskTowns.getSettings().adminTownName)) {
            chunkTypeString = "Admin Claim Ⓐ";
        }

        String townPopup = "<div class=\"infowindow\"><span style=\"font-weight:bold; color:%COLOR%;\">%TOWN_NAME%</span><br/><span style=\"font-style:italic;\">%CLAIM_TYPE%</span><br/><span style=\"font-weight:bold; color:%COLOR%\">Chunk: </span>%CHUNK%<br/><span style=\"font-weight:bold; color:%COLOR%\">Claimed: </span>%CLAIM_TIME%<br/><span style=\"font-weight:bold; color:%COLOR%\">By: </span>%CLAIMER%</div>";
        townPopup = townPopup.replace("%COLOR%", escapeHtml(Town.getTownColorHex(claimedChunk.getTown())));
        townPopup = townPopup.replace("%CLAIM_TYPE%", escapeHtml(chunkTypeString));
        townPopup = townPopup.replace("%TOWN_NAME%", escapeHtml(claimedChunk.getTown()));
        townPopup = townPopup.replace("%CHUNK%", escapeHtml(claimedChunk.getChunkX() + ", " + claimedChunk.getChunkZ()));
        townPopup = townPopup.replace("%CLAIM_TIME%", escapeHtml(claimedChunk.getFormattedClaimTime()));
        if (HuskTowns.getPlayerCache().getPlayerUsername(claimedChunk.getClaimerUUID()) != null) {
            townPopup = townPopup.replace("%CLAIMER%", escapeHtml(HuskTowns.getPlayerCache().getPlayerUsername(claimedChunk.getClaimerUUID())));
        } else {
            townPopup = townPopup.replace("%CLAIMER%", "A citizen");
        }
        return townPopup;
    }

    public String getClaimMarkerId(ClaimedChunk claimedChunk) {
        return "husktowns.claim." + claimedChunk.getServer() + "." + claimedChunk.getWorld() + "." + claimedChunk.getChunkX() + "." + claimedChunk.getChunkZ();
    }

}
