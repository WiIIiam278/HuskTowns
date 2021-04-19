package me.william278.bungeetowny;

import me.william278.bungeetowny.command.TownCommand;
import me.william278.bungeetowny.config.Settings;
import me.william278.bungeetowny.data.sql.Database;
import me.william278.bungeetowny.data.sql.MySQL;
import me.william278.bungeetowny.data.sql.SQLite;
import me.william278.bungeetowny.listeners.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.util.logging.Level;

public final class HuskTowns extends JavaPlugin {

    // Instance handling
    private static HuskTowns instance;
    public static HuskTowns getInstance() {
        return instance;
    }
    private static void setInstance(HuskTowns plugin) {
        instance = plugin;
    }

    // Plugin configuration handling
    private static Settings settings;
    public void reloadConfigFile() {
        reloadConfig();
        settings = new Settings(getConfig());
    }
    public static Settings getSettings() {
        return settings;
    }

    // Database handling
    private static Database database;
    public static Connection getConnection() {
        return database.getConnection();
    }

    // Initialise the database
    private void initializeDatabase() {
        String dataStorageType = HuskTowns.getSettings().getDatabaseType().toLowerCase();
        switch (dataStorageType) {
            case "mysql":
                database = new MySQL(getInstance());
                database.load();
                break;
            case "sqlite":
                database = new SQLite(getInstance());
                database.load();
                break;
            default:
                getLogger().log(Level.WARNING, "An invalid data storage type was specified in config.yml; defaulting to SQLite");
                database = new SQLite(getInstance());
                database.load();
                break;
        }
    }

    // Register plugin commands
    private void registerCommands() {
        new TownCommand().register(getCommand("town"));
    }

    @Override
    public void onLoad() {
        // Set instance for cross-class referencing
        setInstance(this);

        // Retrieve configuration from file
        reloadConfigFile();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Enabling HuskTowns...");

        // Fetch plugin messages from file
        MessageManager.loadMessages();

        // Initialise database
        initializeDatabase();

        // Register events via listener class
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        // Register commands
        registerCommands();

        getLogger().info("Enabled HuskTowns successfully.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Disabled HuskTowns successfully.");
    }
}
