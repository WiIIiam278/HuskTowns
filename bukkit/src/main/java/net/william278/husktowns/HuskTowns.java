package net.william278.husktowns;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.william278.desertwell.Version;
import net.william278.husktowns.command.*;
import net.william278.husktowns.config.Settings;
import net.william278.husktowns.data.message.pluginmessage.PluginMessageReceiver;
import net.william278.husktowns.data.message.redis.RedisReceiver;
import net.william278.husktowns.data.sql.Database;
import net.william278.husktowns.data.sql.MySQL;
import net.william278.husktowns.data.sql.SQLite;
import net.william278.husktowns.hook.economy.RedisEconomyHook;
import net.william278.husktowns.hook.luckperms.LuckPermsHook;
import net.william278.husktowns.hook.map.BlueMap;
import net.william278.husktowns.hook.map.DynMap;
import net.william278.husktowns.hook.HuskHomesHook;
import net.william278.husktowns.hook.economy.VaultHook;
import net.william278.husktowns.hook.map.Map;
import net.william278.husktowns.hook.map.SquareMap;
import net.william278.husktowns.listener.EventListener;
import net.william278.husktowns.cache.TownBonusesCache;
import net.william278.husktowns.town.TownInvite;
import net.william278.husktowns.cache.ClaimCache;
import net.william278.husktowns.cache.PlayerCache;
import net.william278.husktowns.cache.TownDataCache;
import net.william278.husktowns.util.PlayerList;
import net.william278.desertwell.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public final class HuskTowns extends JavaPlugin {

    private static final int SPIGOT_RESOURCE_ID = 92672;
    private static final int METRICS_PLUGIN_ID = 11265;

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

    public static PlayerList getPlayerList() {
        return playerList;
    }

    // Plugin configuration handling
    private static Settings settings;

    private VaultHook economyHook;

    public void reloadSettings() throws IOException {
        settings = new Settings(YamlDocument.create(new File(getDataFolder(), "config.yml"),
                Objects.requireNonNull(getResource("config.yml")),
                GeneralSettings.builder().setUseDefaults(false).build(),
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.builder().setEncoding(DumperSettings.Encoding.UNICODE).build(),
                UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build()));
    }

    public static Settings getSettings() {
        return settings;
    }

    // LuckPerms handler
    private static LuckPermsHook luckPermsHook = null;

    public static void initializeLuckPermsIntegration() {
        if (luckPermsHook == null && getPlayerCache().hasLoaded() && getClaimCache().hasLoaded() && getTownDataCache().hasLoaded() && getTownBonusesCache().hasLoaded()) {
            Bukkit.getScheduler().runTaskAsynchronously(getInstance(), () -> {
                if ((Bukkit.getPluginManager().getPlugin("LuckPerms") != null) && (getSettings().doLuckPerms)) {
                    luckPermsHook = new LuckPermsHook();
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
        switch (HuskTowns.getSettings().databaseType) {
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
        new EvictCommand().register(getCommand("evict")).setTabCompleter(townMemberTab);

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
        if (getSettings().messengerType == Settings.MessengerType.PLUGIN_MESSAGE) {
            Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PluginMessageReceiver());
        } else {
            RedisReceiver.initialize();
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

        // Retrieve configuration from file
        try {
            reloadSettings();
        } catch (IOException e) {
            this.setEnabled(false);
            return;
        }

        // Check for updates
        if (getSettings().startupCheckForUpdates) {
            getLogger().log(Level.INFO, "Checking for updates...");
            getLatestVersionIfOutdated().thenAccept(newestVersion ->
                    newestVersion.ifPresent(newVersion -> getLogger().log(Level.WARNING,
                            "An update is available for HuskHomes, v" + newVersion
                            + " (Currently running v" + getDescription().getVersion() + ")")));
        }

        // Fetch plugin messages from file
        try {
            MessageManager.loadMessages(getSettings().language);
        } catch (IOException e) {
            this.setEnabled(false);
            return;
        }

        // Initialise database
        initializeDatabase();

        // Set up the map integration
        if (getSettings().doMapIntegration) {
            switch (getSettings().mapIntegrationPlugin.toLowerCase()) {
                case "dynmap" -> {
                    map = new DynMap();
                    map.initialize();
                }
                case "bluemap" -> {
                    map = new BlueMap();
                    map.initialize();
                }
                case "pl3xmap", "squaremap" -> {
                    map = new SquareMap();
                    map.initialize();
                }
                default -> {
                    getSettings().doMapIntegration = false;
                    getLogger().warning("An invalid map integration type was specified; disabling map integration.");
                }
            }
        }


        // Setup Economy integration
        if(Bukkit.getPluginManager().getPlugin("RedisEconomy")!=null){
            economyHook = new RedisEconomyHook(this);
        }else{
            economyHook = new VaultHook(this);
        }

        getSettings().doEconomy = (getSettings().doEconomy && economyHook.initialize());

        // Setup HuskHomes integration
        getSettings().doHuskHomes = (getSettings().doHuskHomes && HuskHomesHook.initialize());

        // Initialise caches & cached data
        initializeCaches();

        // Register events via listener classes
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        if (getSettings().doHuskHomes && getSettings().disableHuskHomesSetHomeInOtherTown) {
            try {
                getServer().getPluginManager().registerEvents(new HuskHomesHook(), this);
            } catch (IllegalPluginAccessException e) {
                getLogger().log(Level.WARNING, "Your version of HuskHomes is not compatible with HuskTowns.\nPlease update to HuskHomes v1.4.2+; certain features will not work.");
            }
        }

        // Register commands
        registerCommands();

        // Register messaging channels (Redis/Plugin Message)
        if (getSettings().doBungee) {
            setupMessagingChannels();
        }

        // Setup player list
        playerList = new PlayerList();
        playerList.initialize();

        // bStats initialisation
        try {
            Metrics metrics = new Metrics(this, METRICS_PLUGIN_ID);
            metrics.addCustomChart(new SimplePie("bungee_mode", () -> Boolean.toString(getSettings().doBungee)));
            metrics.addCustomChart(new SimplePie("language", () -> getSettings().language.toLowerCase()));
            metrics.addCustomChart(new SimplePie("database_type", () -> getSettings().databaseType.toString().toLowerCase()));
            metrics.addCustomChart(new SimplePie("using_economy", () -> Boolean.toString(getSettings().doEconomy)));
            metrics.addCustomChart(new SimplePie("using_town_chat", () -> Boolean.toString(getSettings().doTownChat)));
            metrics.addCustomChart(new SimplePie("using_map", () -> Boolean.toString(getSettings().doMapIntegration)));
            metrics.addCustomChart(new SimplePie("map_type", () -> getSettings().mapIntegrationPlugin.toLowerCase()));
            if (getSettings().doBungee) {
                metrics.addCustomChart(new SimplePie("messenger_type", () -> getSettings().messengerType.toString().toLowerCase()));
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
        if (luckPermsHook != null) {
            luckPermsHook.unRegisterProviders();
        }

        // Close redis pool
        if (getSettings().doBungee) {
            if (getSettings().messengerType == Settings.MessengerType.REDIS) {
                RedisReceiver.terminate();
            }
        }

        // Plugin shutdown logic
        getLogger().info("Disabled HuskTowns version " + this.getDescription().getVersion() + " successfully.");
    }

    /**
     * Returns a future returning the latest plugin {@link Version} if the plugin is out-of-date
     *
     * @return a {@link CompletableFuture} returning the latest {@link Version} if the current one is out-of-date
     */
    public CompletableFuture<Optional<Version>> getLatestVersionIfOutdated() {
        final Version currentVersion = Version.fromString(getDescription().getVersion(), "-");
        final UpdateChecker updateChecker = UpdateChecker.create(currentVersion, SPIGOT_RESOURCE_ID);
        return updateChecker.isUpToDate().thenApply(upToDate -> {
            if (upToDate) {
                return Optional.empty();
            } else {
                return Optional.of(updateChecker.getLatestVersion().join());
            }
        });
    }
    public VaultHook getEconomyHook() {
        return economyHook;
    }
}
