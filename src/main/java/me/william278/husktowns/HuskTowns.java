package me.william278.husktowns;

import me.william278.husktowns.command.*;
import me.william278.husktowns.config.Settings;
import me.william278.husktowns.data.sql.Database;
import me.william278.husktowns.data.sql.MySQL;
import me.william278.husktowns.data.sql.SQLite;
import me.william278.husktowns.listener.EventListener;
import me.william278.husktowns.listener.PluginMessageListener;
import me.william278.husktowns.object.cache.ClaimCache;
import me.william278.husktowns.object.cache.PlayerCache;
import org.bukkit.Bukkit;
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

    // Claimed chunk cache
    private static ClaimCache claimCache;
    public static ClaimCache getClaimCache() { return claimCache; }

    // Player cache
    private static PlayerCache playerCache;
    public static PlayerCache getPlayerCache() { return playerCache; }

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

    // Register Plugin Message channels
    private void registerPluginMessageChannels() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PluginMessageListener());
    }

    // Register plugin commands
    private void registerCommands() {
        new TownCommand().register(getCommand("town"));
        new ClaimCommand().register(getCommand("claim"));
        new ClaimListCommand().register(getCommand("claimlist"));
        new UnclaimCommand().register(getCommand("unclaim"));
        new MapCommand().register(getCommand("map"));
        new PromoteCommand().register(getCommand("promote"));
        new DemoteCommand().register(getCommand("demote"));
    }

    @Override
    public void onLoad() {
        // Set instance for cross-class referencing
        setInstance(this);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Enabling HuskTowns version " + this.getDescription().getVersion());

        // Retrieve configuration from file
        saveDefaultConfig();
        reloadConfigFile();

        // Fetch plugin messages from file
        MessageManager.loadMessages(getSettings().getLanguage());

        // Initialise database
        initializeDatabase();

        // Initialise caches
        claimCache = new ClaimCache();
        playerCache = new PlayerCache();

        // Register events via listener class
        getServer().getPluginManager().registerEvents(new EventListener(), this);

        // Register commands
        registerCommands();

        // Register Plugin Message channels
        if (getSettings().doBungee()) {
            registerPluginMessageChannels();
        }

        getLogger().info("Enabled HuskTowns version " + this.getDescription().getVersion() + " successfully.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Disabled HuskTowns version " + this.getDescription().getVersion() + " successfully.");
    }
}
