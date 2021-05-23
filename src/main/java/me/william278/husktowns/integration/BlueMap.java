package me.william278.husktowns.integration;

import me.william278.husktowns.HuskTowns;
import org.bukkit.plugin.Plugin;

public class BlueMap {

    private static Plugin blueMap;
    private static final HuskTowns plugin = HuskTowns.getInstance();

   // todo https://github.com/BlueMap-Minecraft/BlueMapAPI/wiki

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
