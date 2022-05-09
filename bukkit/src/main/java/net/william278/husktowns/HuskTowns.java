package net.william278.husktowns;

import net.william278.husktowns.commands.*;
import net.william278.husktowns.config.Settings;
import net.william278.husktowns.data.message.pluginmessage.PluginMessageReceiver;
import net.william278.husktowns.data.message.redis.RedisReceiver;
import net.william278.husktowns.data.sql.Database;
import net.william278.husktowns.data.sql.MySQL;
import net.william278.husktowns.data.sql.SQLite;
import net.william278.husktowns.integrations.luckperms.LuckPermsIntegration;
import net.william278.husktowns.integrations.map.BlueMap;
import net.william278.husktowns.integrations.map.DynMap;
import net.william278.husktowns.integrations.HuskHomesIntegration;
import net.william278.husktowns.integrations.VaultIntegration;
import net.william278.husktowns.integrations.map.Map;
import net.william278.husktowns.integrations.map.SquareMap;
import net.william278.husktowns.listener.EventListener;
import net.william278.husktowns.cache.TownBonusesCache;
import net.william278.husktowns.town.TownInvite;
import net.william278.husktowns.cache.ClaimCache;
import net.william278.husktowns.cache.PlayerCache;
import net.william278.husktowns.cache.TownDataCache;
import net.william278.husktowns.util.PlayerList;
import net.william278.husktowns.util.UpdateChecker;
import net.william278.husktowns.util.UpgradeUtil;
import net.william278.husktowns.commands.*;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
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

    // Player list managing
    private static PlayerList playerList;

    public static PlayerList getPlayerList() { return playerList; }

    // Plugin configuration handling
    private static Settings settings;

    public void reloadConfigFile() {
        reloadConfig();
        settings = new Settings(getConfig());
    }

    public static Settings getSettings() {
        return settings;
    }

    // LuckPerms handler
    private static LuckPermsIntegration luckPermsIntegration = null;

    public static void initializeLuckPermsIntegration() {
        if (luckPermsIntegration == null && getPlayerCache().hasLoaded() && getClaimCache().hasLoaded() && getTownDataCache().hasLoaded() && getTownBonusesCache().hasLoaded()) {
            Bukkit.getScheduler().runTaskAsynchronously(getInstance(), () -> {
                if ((Bukkit.getPluginManager().getPlugin("LuckPerms") != null) && (getSettings().doLuckPerms())) {
                    luckPermsIntegration = new LuckPermsIntegration();
                }
            });
        }
    }

    // Database handling
    private static Database database;

    public static Connection getConnection() throws SQLException {
        return database.getConnection();
    }

    // Claimed chunk cache
    private static ClaimCache claimCache;

    public static ClaimCache getClaimCache() {
        return claimCache;
    }

    public static void setClaimCache(ClaimCache cache) {
        claimCache = cache;
    }

    // Town messages cache
    private static TownDataCache townDataCache;

    public static TownDataCache getTownDataCache() {
        return townDataCache;
    }

    public static void setTownDataCache(TownDataCache cache) {
        townDataCache = cache;
    }

    // Player cache
    private static PlayerCache playerCache;

    public static PlayerCache getPlayerCache() {
        return playerCache;
    }

    public static void setPlayerCache(PlayerCache cache) {
        playerCache = cache;
    }


    // Town bonuses cache
    private static TownBonusesCache townBonusesCache;

    public static TownBonusesCache getTownBonusesCache() {
        return townBonusesCache;
    }

    public static void setTownBonusesCache(TownBonusesCache cache) {
        townBonusesCache = cache;
    }

    // Map integration, if being used
    private static Map map;

    public static Map getMap() {
        return map;
    }

    // Current invites
    public static HashMap<UUID, TownInvite> invites = new HashMap<>();

    // Players with town chat toggled on
    public static HashSet<UUID> townChatPlayers = new HashSet<>();

    // Players who are overriding claims
    public static HashSet<UUID> ignoreClaimPlayers = new HashSet<>();

    // Initialise the database
    private void initializeDatabase() {
        switch (HuskTowns.getSettings().getDatabaseType()) {
            case MYSQL -> database = new MySQL(getInstance());
            case SQLITE -> database = new SQLite(getInstance());
        }
        database.load();
    }

    // Reload or initialise the caches
    public static void initializeCaches() {
        setClaimCache(new ClaimCache());
        setPlayerCache(new PlayerCache());
        setTownDataCache(new TownDataCache());
        setTownBonusesCache(new TownBonusesCache());
        claimCache.reload();
        playerCache.reload();
        townDataCache.reload();
        townBonusesCache.reload();
    }

    // Register plugin commands and tab completion
    private void registerCommands() {
        CommandBase.EmptyTab emptyTab = new CommandBase.EmptyTab();
        new MapCommand().register(getCommand("map")).setTabCompleter(emptyTab);
        new FarmCommand().register(getCommand("farm")).setTabCompleter(emptyTab);
        new AutoClaimCommand().register(getCommand("autoclaim")).setTabCompleter(emptyTab);
        new AdminClaimCommand().register(getCommand("adminclaim")).setTabCompleter(emptyTab);
        new TownChatCommand().register(getCommand("townchat")).setTabCompleter(emptyTab);
        new IgnoreClaimsCommand().register(getCommand("ignoreclaims")).setTabCompleter(emptyTab);

        ClaimCommand.ClaimTab claimTab = new ClaimCommand.ClaimTab();
        new ClaimCommand().register(getCommand("claim")).setTabCompleter(claimTab);

        UnClaimCommand.UnClaimCommandTab unClaimTab = new UnClaimCommand.UnClaimCommandTab();
        new UnClaimCommand().register(getCommand("unclaim")).setTabCompleter(unClaimTab);

        TownCommand.TownTab townTab = new TownCommand.TownTab();
        new TownCommand().register(getCommand("town")).setTabCompleter(townTab);

        ClaimListCommand.TownListTab townListTab = new ClaimListCommand.TownListTab();
        new ClaimListCommand().register(getCommand("claimlist")).setTabCompleter(townListTab);

        PromoteCommand.TownMemberTab townMemberTab = new PromoteCommand.TownMemberTab();
        new PromoteCommand().register(getCommand("promote")).setTabCompleter(townMemberTab);
        new DemoteCommand().register(getCommand("demote")).setTabCompleter(townMemberTab);
        new TransferCommand().register(getCommand("transfer")).setTabCompleter(townMemberTab);

        HuskTownsCommand.HuskTownsCommandTab huskTownsCommandTab = new HuskTownsCommand.HuskTownsCommandTab();
        new HuskTownsCommand().register(getCommand("husktowns")).setTabCompleter(huskTownsCommandTab);

        PlotCommand.PlotTab plotTab = new PlotCommand.PlotTab();
        new PlotCommand().register(getCommand("plot")).setTabCompleter(plotTab);

        TownListCommand.TownListCommandTab townListCommandTab = new TownListCommand.TownListCommandTab();
        new TownListCommand().register(getCommand("townlist")).setTabCompleter(townListCommandTab);

        AdminTownCommand.AdminTownCommandTab adminTownCommandTab = new AdminTownCommand.AdminTownCommandTab();
        new AdminTownCommand().register(getCommand("admintown")).setTabCompleter(adminTownCommandTab);

        new TownBonusCommand().register(getCommand("townbonus"));
        new InviteCommand().register(getCommand("invite"));
    }

    private void setupMessagingChannels() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        if (getSettings().getMessengerType() == Settings.MessengerType.PLUGIN_MESSAGE) {
            Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PluginMessageReceiver());
        } else {
            RedisReceiver.listen();
        }
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
        getConfig().options().copyDefaults(true);
        saveConfig();
        reloadConfigFile();

        // Fetch plugin messages from file
        MessageManager.loadMessages(getSettings().getLanguage());

        // Initialise database
        initializeDatabase();

        // Check for system upgrades needed
        UpgradeUtil.checkNeededUpgrades();

        // Set up the map integration
        if (getSettings().doMapIntegration()) {
            switch (getSettings().getMapIntegrationPlugin().toLowerCase(Locale.ROOT)) {
                case "dynmap" -> {
                    map = new DynMap();
                    map.initialize();
                }
                case "bluemap" -> {
                    map = new BlueMap();
                    map.initialize();
                }
                case "pl3xmap" -> {
                    map = new SquareMap();
                    map.initialize();
                }
                default -> {
                    getSettings().setDoMapIntegration(false);
                    getLogger().warning("An invalid map integration type was specified; disabling map integration.");
                }
            }
        }

        // Setup Economy integration
        getSettings().setDoEconomy(VaultIntegration.initialize());

        // Setup HuskHomes integration
        getSettings().setHuskHomes(HuskHomesIntegration.initialize());

        // Initialise caches & cached data
        initializeCaches();

        // Register events via listener classes
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        if (getSettings().doHuskHomes() && getSettings().disableHuskHomesSetHomeInOtherTown()) {
            try {
                getServer().getPluginManager().registerEvents(new HuskHomesIntegration(), this);
            } catch (IllegalPluginAccessException e) {
                getLogger().log(Level.WARNING, "Your version of HuskHomes is not compatible with HuskTowns.\nPlease update to HuskHomes v1.4.2+; certain features will not work.");
            }
        }

        // Register commands
        registerCommands();

        // Register messaging channels (Redis/Plugin Message)
        if (getSettings().doBungee()) {
            setupMessagingChannels();
        }

        // Setup player list
        playerList = new PlayerList();
        playerList.initialize();

        // bStats initialisation
        try {
            Metrics metrics = new Metrics(this, METRICS_PLUGIN_ID);
            metrics.addCustomChart(new SimplePie("bungee_mode", () -> Boolean.toString(getSettings().doBungee())));
            metrics.addCustomChart(new SimplePie("language", () -> getSettings().getLanguage().toLowerCase()));
            metrics.addCustomChart(new SimplePie("database_type", () -> getSettings().getDatabaseType().toString().toLowerCase()));
            metrics.addCustomChart(new SimplePie("using_economy", () -> Boolean.toString(getSettings().doEconomy())));
            metrics.addCustomChart(new SimplePie("using_town_chat", () -> Boolean.toString(getSettings().doTownChat())));
            metrics.addCustomChart(new SimplePie("using_map", () -> Boolean.toString(getSettings().doMapIntegration())));
            metrics.addCustomChart(new SimplePie("map_type", () -> getSettings().getMapIntegrationPlugin().toLowerCase()));
            if (getSettings().doBungee()) {
                metrics.addCustomChart(new SimplePie("messenger_type", () -> getSettings().getMessengerType().toString().toLowerCase()));
            }
        } catch (Exception e) {
            getLogger().warning("An exception occurred initialising metrics; skipping.");
        }

        getLogger().info("Enabled HuskTowns version " + this.getDescription().getVersion() + " successfully.");
    }

    @Override
    public void onDisable() {
        // Cancel remaining tasks
        Bukkit.getServer().getScheduler().cancelTasks(this);

        // Unregister context calculators (LuckPerms)
        if (luckPermsIntegration != null) {
            luckPermsIntegration.unRegisterProviders();
        }

        // Plugin shutdown logic
        getLogger().info("Disabled HuskTowns version " + this.getDescription().getVersion() + " successfully.");
    }
}
