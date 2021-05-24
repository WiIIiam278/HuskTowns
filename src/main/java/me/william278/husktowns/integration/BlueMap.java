package me.william278.husktowns.integration;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.marker.MarkerSet;
import me.william278.husktowns.HuskTowns;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.Optional;

public class BlueMap {

    private static Plugin blueMap;
    private static final HuskTowns plugin = HuskTowns.getInstance();

   // todo https://github.com/BlueMap-Minecraft/BlueMapAPI/wiki

    /*public static Optional<MarkerSet> getMarkerSet() {
        BlueMapAPI.getInstance().ifPresent(api -> {
            //code executed when the api is enabled (skipped if the api is not enabled)
            try {
                Optional<MarkerSet> markerSet;
                markerSet = api.getMarkerAPI().getMarkerSet("Towns");
                if (markerSet.isPresent()) {
                    return markerSet;
                } else {
                    return api.getMarkerAPI().createMarkerSet("Towns");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return Optional.empty();
    }*/

    public static void initialize() {
        if (HuskTowns.getSettings().doBlueMap()) {
            blueMap = plugin.getServer().getPluginManager().getPlugin("BlueMap");
            if (blueMap == null) {
                HuskTowns.getSettings().setDoDynMap(false);
                plugin.getConfig().set("integrations.bluemap.enabled", false);
                plugin.saveConfig();
                return;
            }
            if (!blueMap.isEnabled()) {
                HuskTowns.getSettings().setDoDynMap(false);
                plugin.getConfig().set("integrations.bluemap.enabled", false);
                plugin.saveConfig();
                return;
            }
            plugin.getLogger().info("Enabled BlueMap integration!");
        }
    }

}
