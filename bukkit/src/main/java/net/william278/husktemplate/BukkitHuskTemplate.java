package net.william278.husktemplate;

import org.bukkit.plugin.java.JavaPlugin;

public class BukkitHuskTemplate extends JavaPlugin implements HuskTemplate {

    // Instance of the plugin
    private static BukkitHuskTemplate instance;

    public static BukkitHuskTemplate getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        // Set the instance
        instance = this;
    }

    @Override
    public void onEnable() {
        // todo Initialize HuskTemplate
    }
}