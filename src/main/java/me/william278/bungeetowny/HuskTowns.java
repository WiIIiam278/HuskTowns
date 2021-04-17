package me.william278.bungeetowny;

import me.william278.bungeetowny.config.Settings;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class HuskTowns extends JavaPlugin {

    private static HuskTowns instance;
    public static HuskTowns getInstance() {
        return instance;
    }
    private static void setInstance(HuskTowns plugin) {
        instance = plugin;
    }

    private static Settings settings;

    public void reloadConfig() {
        settings = new Settings(getConfig());
    }

    public static Settings getSettings() {
        return settings;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        // Set instance
        setInstance(this);

        // Retrieve config
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
