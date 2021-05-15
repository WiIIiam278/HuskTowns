package me.william278.husktowns;

import me.william278.husktowns.command.*;
import me.william278.husktowns.config.Settings;
import me.william278.husktowns.data.sql.Database;
import me.william278.husktowns.data.sql.MySQL;
import me.william278.husktowns.data.sql.SQLite;
import me.william278.husktowns.integration.DynMap;
import me.william278.husktowns.integration.HuskHomes;
import me.william278.husktowns.integration.Vault;
import me.william278.husktowns.listener.EventListener;
import me.william278.husktowns.listener.PluginMessageListener;
import me.william278.husktowns.object.town.Town;
import me.william278.husktowns.object.town.TownInvite;
import me.william278.husktowns.object.cache.ClaimCache;
import me.william278.husktowns.object.cache.PlayerCache;
import me.william278.husktowns.object.cache.TownMessageCache;
import me.william278.husktowns.util.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.util.HashMap;
import java.util.UUID;
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

    // Town messages cache
    private static TownMessageCache townMessageCache;
    public static TownMessageCache getTownMessageCache() { return townMessageCache; }

    // Player cache
    private static PlayerCache playerCache;
    public static PlayerCache getPlayerCache() { return playerCache; }

    // Current invites
    public static HashMap<UUID,TownInvite> invites = new HashMap<>();

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

    // Register plugin commands and tab completers
    private void registerCommands() {
        CommandBase.EmptyTab emptyTab = new CommandBase.EmptyTab();
        new ClaimCommand().register(getCommand("claim")).setTabCompleter(emptyTab);
        new UnClaimCommand().register(getCommand("unclaim")).setTabCompleter(emptyTab);
        new MapCommand().register(getCommand("map")).setTabCompleter(emptyTab);
        new FarmCommand().register(getCommand("farm")).setTabCompleter(emptyTab);
        new AutoClaimCommand().register(getCommand("autoclaim")).setTabCompleter(emptyTab);
        new AdminClaimCommand().register(getCommand("adminclaim")).setTabCompleter(emptyTab);
        new TownChatCommand().register(getCommand("townchat")).setTabCompleter(emptyTab);

        TownCommand.TownTab townTab = new TownCommand.TownTab();
        new TownCommand().register(getCommand("town")).setTabCompleter(townTab);

        ClaimListCommand.TownListTab townListTab = new ClaimListCommand.TownListTab();
        new ClaimListCommand().register(getCommand("claimlist")).setTabCompleter(townListTab);

        PromoteCommand.TownMemberTab townMemberTab = new PromoteCommand.TownMemberTab();
        new PromoteCommand().register(getCommand("promote")).setTabCompleter(townMemberTab);
        new DemoteCommand().register(getCommand("demote")).setTabCompleter(townMemberTab);
        new EvictCommand().register(getCommand("evict")).setTabCompleter(townMemberTab);
        new TransferCommand().register(getCommand("transfer")).setTabCompleter(townMemberTab);

        HuskTownsCommand.HuskTownsTab huskTownsTab = new HuskTownsCommand.HuskTownsTab();
        new HuskTownsCommand().register(getCommand("husktowns")).setTabCompleter(huskTownsTab);

        PlotCommand.PlotTab plotTab = new PlotCommand.PlotTab();
        new PlotCommand().register(getCommand("plot")).setTabCompleter(plotTab);

        TownListCommand.TownListCommandTab townListCommandTab = new TownListCommand.TownListCommandTab();
        new TownListCommand().register(getCommand("townlist")).setTabCompleter(townListCommandTab);

        new InviteCommand().register(getCommand("invite"));
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

        // Check for updates
        new UpdateChecker(this).logToConsole();

        // Retrieve configuration from file
        saveDefaultConfig();
        reloadConfigFile();

        // Fetch plugin messages from file
        MessageManager.loadMessages(getSettings().getLanguage());

        // Initialise database
        initializeDatabase();

        // Setup Dynmap integration
        DynMap.initialize();

        // Setup Economy integration
        getSettings().setDoEconomy(Vault.initialize());

        // Setup HuskHomes integration
        getSettings().setHuskHomes(HuskHomes.initialize());

        // Initialise caches
        claimCache = new ClaimCache();
        playerCache = new PlayerCache();
        townMessageCache = new TownMessageCache();

        // Register events via listener classes
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        if (getSettings().doHuskHomes() && getSettings().disableHuskHomesSetHomeInOtherTown()) {
            try {
                getServer().getPluginManager().registerEvents(new HuskHomes(), this);
            } catch (IllegalPluginAccessException e) {
                getLogger().log(Level.WARNING, "Your version of HuskHomes is not compatible with HuskTowns.\nPlease update to HuskHomes v1.4.2+; certain features will not work.");
            }
        }

        // Register commands
        registerCommands();

        // Register Plugin Message channels
        if (getSettings().doBungee()) {
            registerPluginMessageChannels();
        }

        // Enable bStats integration
        //new MetricsLite(this, 11265);

        getLogger().info("Enabled HuskTowns version " + this.getDescription().getVersion() + " successfully.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Disabled HuskTowns version " + this.getDescription().getVersion() + " successfully.");
    }
}
