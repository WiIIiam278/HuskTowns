package me.william278.husktowns;

import me.william278.husktowns.commands.*;
import me.william278.husktowns.config.Settings;
import me.william278.husktowns.data.sql.Database;
import me.william278.husktowns.data.sql.MySQL;
import me.william278.husktowns.data.sql.SQLite;
import me.william278.husktowns.integrations.map.BlueMap;
import me.william278.husktowns.integrations.map.DynMap;
import me.william278.husktowns.integrations.HuskHomes;
import me.william278.husktowns.integrations.Vault;
import me.william278.husktowns.integrations.map.Map;
import me.william278.husktowns.integrations.map.Pl3xMap;
import me.william278.husktowns.listeners.EventListener;
import me.william278.husktowns.listeners.PluginMessageListener;
import me.william278.husktowns.object.cache.TownBonusesCache;
import me.william278.husktowns.object.town.TownInvite;
import me.william278.husktowns.object.cache.ClaimCache;
import me.william278.husktowns.object.cache.PlayerCache;
import me.william278.husktowns.object.cache.TownMessageCache;
import me.william278.husktowns.util.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;

public final class HuskTowns extends JavaPlugin {

    public static final int METRICS_PLUGIN_ID = 11265;

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

    // Town bonuses cache
    private static TownBonusesCache townBonusesCache;
    public static TownBonusesCache getTownBonusesCache() { return townBonusesCache; }

    // Map integration, if being used
    private static Map map;
    public static Map getMap() {
        return map;
    }

    // Current invites
    public static HashMap<UUID,TownInvite> invites = new HashMap<>();

    // Players with town chat toggled on
    public static HashSet<UUID> townChatPlayers = new HashSet<>();

    // Players who are overriding claims
    public static HashSet<UUID> ignoreClaimPlayers = new HashSet<>();

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
        new IgnoreClaimsCommand().register(getCommand("ignoreclaims")).setTabCompleter(emptyTab);

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

        AdminTownCommand.AdminTownCommandTab adminTownCommandTab = new AdminTownCommand.AdminTownCommandTab();
        new AdminTownCommand().register(getCommand("admintown")).setTabCompleter(adminTownCommandTab);

        new TownBonusCommand().register(getCommand("townbonus"));
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

        // Setup the map integration
        if (getSettings().doMapIntegration()) {
            switch (getSettings().getMapIntegrationPlugin().toLowerCase(Locale.ROOT)) {
                case "dynmap":
                    map = new DynMap();
                    map.initialize();
                    break;
                case "bluemap":
                    map = new BlueMap();
                    map.initialize();
                    break;
                case "pl3xmap":
                    map = new Pl3xMap();
                    map.initialize();
                    break;
                default:
                    getSettings().setDoMapIntegration(false);
                    getLogger().warning("An invalid map integration type was specified; disabling map integration.");
                    break;
            }
        }

        // Setup Economy integration
        getSettings().setDoEconomy(Vault.initialize());

        // Setup HuskHomes integration
        getSettings().setHuskHomes(HuskHomes.initialize());

        // Initialise caches
        claimCache = new ClaimCache();
        playerCache = new PlayerCache();
        townMessageCache = new TownMessageCache();
        townBonusesCache = new TownBonusesCache();

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

        // bStats initialisation
        try {
            Metrics metrics = new Metrics(this, METRICS_PLUGIN_ID);
            metrics.addCustomChart(new SimplePie("bungee_mode", () -> Boolean.toString(getSettings().doBungee())));
            metrics.addCustomChart(new SimplePie("language", () -> getSettings().getLanguage().toLowerCase(Locale.ROOT)));
            metrics.addCustomChart(new SimplePie("database_type", () -> getSettings().getDatabaseType().toLowerCase(Locale.ROOT)));
            metrics.addCustomChart(new SimplePie("using_economy", () -> Boolean.toString(getSettings().doEconomy())));
            metrics.addCustomChart(new SimplePie("using_town_chat", () -> Boolean.toString(getSettings().doTownChat())));
            metrics.addCustomChart(new SimplePie("using_map", () -> Boolean.toString(getSettings().doMapIntegration())));
            metrics.addCustomChart(new SimplePie("map_type", () -> getSettings().getMapIntegrationPlugin().toLowerCase(Locale.ROOT)));
        } catch (Exception e) {
            getLogger().warning("An exception occurred initialising metrics; skipping.");
        }

        getLogger().info("Enabled HuskTowns version " + this.getDescription().getVersion() + " successfully.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Disabled HuskTowns version " + this.getDescription().getVersion() + " successfully.");
    }
}
