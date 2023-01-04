package net.william278.husktowns;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.william278.desertwell.Version;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.command.AdminTownCommand;
import net.william278.husktowns.command.BukkitCommand;
import net.william278.husktowns.command.HuskTownsCommand;
import net.william278.husktowns.command.TownCommand;
import net.william278.husktowns.config.*;
import net.william278.husktowns.database.Database;
import net.william278.husktowns.events.*;
import net.william278.husktowns.hook.*;
import net.william278.husktowns.listener.BukkitEventListener;
import net.william278.husktowns.manager.Manager;
import net.william278.husktowns.network.Broker;
import net.william278.husktowns.network.PluginMessageBroker;
import net.william278.husktowns.town.Invite;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.ConsoleUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
import net.william278.husktowns.util.Validator;
import net.william278.husktowns.visualizer.Visualizer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public final class BukkitHuskTowns extends JavaPlugin implements HuskTowns, PluginMessageListener {

    private static BukkitHuskTowns instance;
    private BukkitAudiences audiences;
    private Settings settings;
    private Locales locales;
    private Roles roles;
    private Presets presets;
    private Levels levels;
    private Server server;
    private Database database;
    private Manager manager;
    @Nullable
    private Broker broker;
    private Validator validator;
    private SpecialTypes specialTypes;
    private Map<UUID, Deque<Invite>> invites;
    private Map<UUID, Preferences> userPreferences;
    private Map<UUID, Visualizer> visualizers;
    private List<Town> towns;
    private Map<UUID, ClaimWorld> claimWorlds;
    private List<Hook> hooks;
    private boolean loaded = false;

    @SuppressWarnings("unused")
    public BukkitHuskTowns() {
        super();
    }

    @SuppressWarnings("unused")
    private BukkitHuskTowns(@NotNull JavaPluginLoader loader, @NotNull PluginDescriptionFile description,
                            @NotNull File dataFolder, @NotNull File file) {
        super(loader, description, dataFolder, file);
    }

    @NotNull
    public static BukkitHuskTowns getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        // Set the instance
        instance = this;
    }

    @Override
    public void onEnable() {
        // Enable HuskTowns and load configuration
        this.loadConfig();
        this.audiences = BukkitAudiences.create(this);
        this.validator = new Validator(this);
        this.invites = new HashMap<>();
        this.userPreferences = new HashMap<>();
        this.visualizers = new HashMap<>();
        this.hooks = new ArrayList<>();

        // Check for updates
        this.checkForUpdates();

        // Prepare the database and networking system
        this.database = this.loadDatabase();
        this.manager = new Manager(this);
        this.broker = this.loadBroker();

        // Register hooks
        final PluginManager plugins = Bukkit.getPluginManager();
        if (settings.economyHook) {
            if (plugins.getPlugin("RedisEconomy") != null) {
                this.registerHook(new RedisEconomyHook(this));
            } else if (plugins.getPlugin("Vault") != null) {
                this.registerHook(new VaultEconomyHook(this));
            }
        }
        if (settings.webMapPluginHook) {
            if (plugins.getPlugin("BlueMap") != null) {
                this.registerHook(new BlueMapHook(this));
            } else if (plugins.getPlugin("dynmap") != null) {
                this.registerHook(new DynmapHook(this));
            }
        }


        // Load towns and claim worlds
        this.loadData();

        // Prepare commands
        List.of(new HuskTownsCommand(this), new TownCommand(this), new AdminTownCommand(this))
                .forEach(command -> new BukkitCommand(command, this).register());

        // Register event listener
        Bukkit.getPluginManager().registerEvents(new BukkitEventListener(this), this);
        log(Level.INFO, "Enabled HuskTowns v" + getVersion());
    }

    @Override
    public void onDisable() {
        getDatabase().close();
        visualizers.values().forEach(Visualizer::cancel);
        getMessageBroker().ifPresent(Broker::close);
        log(Level.INFO, "Disabled HuskTowns v" + getVersion());
    }

    @Override
    @NotNull
    public Settings getSettings() {
        return settings;
    }

    @Override
    public void setSettings(@NotNull Settings settings) {
        this.settings = settings;
    }

    @Override
    @NotNull
    public Locales getLocales() {
        return locales;
    }

    @Override
    public void setLocales(@NotNull Locales locales) {
        this.locales = locales;
    }

    @Override
    @NotNull
    public Roles getRoles() {
        return roles;
    }

    @Override
    public void setRoles(@NotNull Roles roles) {
        this.roles = roles;
    }

    @Override
    @NotNull
    public Presets getRulePresets() {
        return presets;
    }

    @Override
    public void setRulePresets(@NotNull Presets presets) {
        this.presets = presets;
    }

    @Override
    @NotNull
    public Levels getLevels() {
        return levels;
    }

    @Override
    public void setLevels(@NotNull Levels levels) {
        this.levels = levels;
    }

    @Override
    @NotNull
    public String getServerName() {
        return server != null ? server.getName() : "server";
    }

    @Override
    public void setServer(Server server) {
        this.server = server;
    }

    @Override
    @NotNull
    public Database getDatabase() {
        return database;
    }

    @Override
    @NotNull
    public Manager getManager() {
        return manager;
    }

    @Override
    @NotNull
    public Optional<Broker> getMessageBroker() {
        return Optional.ofNullable(broker);
    }

    @Override
    @NotNull
    public Validator getValidator() {
        return validator;
    }

    @Override
    @NotNull
    public SpecialTypes getSpecialTypes() {
        return specialTypes;
    }

    @Override
    public void setSpecialTypes(@NotNull SpecialTypes specialTypes) {
        this.specialTypes = specialTypes;
    }

    @Override
    @NotNull
    public Map<UUID, Deque<Invite>> getInvites() {
        return invites;
    }

    @Override
    @NotNull
    public Map<UUID, Preferences> getUserPreferences() {
        return userPreferences;
    }

    @Override
    @NotNull
    public List<Town> getTowns() {
        return towns;
    }

    @Override
    public void setTowns(@NotNull List<Town> towns) {
        this.towns = towns;
    }

    @Override
    @NotNull
    public Map<UUID, ClaimWorld> getClaimWorlds() {
        return claimWorlds;
    }

    @Override
    public void setClaimWorlds(@NotNull Map<UUID, ClaimWorld> claimWorlds) {
        this.claimWorlds = claimWorlds;
    }

    @Override
    @NotNull
    public List<World> getWorlds() {
        return Bukkit.getWorlds().stream()
                .map(world -> World.of(world.getUID(), world.getName(), world.getEnvironment().name().toLowerCase()))
                .toList();
    }

    @Override
    @NotNull
    public Map<UUID, Visualizer> getVisualizers() {
        return visualizers;
    }

    @Override
    public void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... throwable) {
        if (throwable.length > 0) {
            getLogger().log(level, message, throwable[0]);
            return;
        }
        getLogger().log(level, message);
    }

    @Override
    @NotNull
    public ConsoleUser getConsole() {
        return new ConsoleUser(audiences.console());
    }

    @Override
    public double getHighestBlockAt(@NotNull Position position) {
        final org.bukkit.World world = Bukkit.getWorld(position.getWorld().getUuid());
        if (world == null) {
            return 64;
        }
        return world.getHighestBlockYAt((int) Math.floor(position.getX()), (int) Math.floor(position.getZ()));
    }

    @Override
    public void initializePluginChannels() {
        Bukkit.getMessenger().registerIncomingPluginChannel(this, PluginMessageBroker.BUNGEE_CHANNEL_ID, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, PluginMessageBroker.BUNGEE_CHANNEL_ID);
    }

    @Override
    @NotNull
    public Version getVersion() {
        return Version.fromString(getDescription().getVersion(), "-");
    }

    @Override
    @NotNull
    public List<? extends OnlineUser> getOnlineUsers() {
        return Bukkit.getOnlinePlayers().stream().map(BukkitUser::adapt).toList();
    }

    @Override
    @NotNull
    public List<Hook> getHooks() {
        return hooks;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (broker != null && broker instanceof PluginMessageBroker pluginMessenger) {
            pluginMessenger.onReceive(channel, BukkitUser.adapt(player), message);
        }
    }

    @NotNull
    public BukkitAudiences getAudiences() {
        return audiences;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public int runAsync(@NotNull Runnable runnable) {
        return Bukkit.getScheduler().runTaskAsynchronously(this, runnable).getTaskId();
    }

    @Override
    public int runSync(@NotNull Runnable runnable) {
        return Bukkit.getScheduler().runTask(this, runnable).getTaskId();
    }

    @Override
    public int runTimedAsync(@NotNull Runnable runnable, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(this, runnable, delay, period).getTaskId();
    }

    @Override
    public void cancelTask(int taskId) {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    private <T extends Event> Optional<T> fireEvent(@NotNull T event) {
        Bukkit.getPluginManager().callEvent(event);
        if (event instanceof Cancellable cancellable) {
            return cancellable.isCancelled() ? Optional.empty() : Optional.of(event);
        }
        return Optional.of(event);
    }

    @Override
    public Optional<ClaimEvent> fireClaimEvent(@NotNull OnlineUser user, @NotNull TownClaim claim) {
        return fireEvent(new ClaimEvent((BukkitUser) user, claim));
    }

    @Override
    public Optional<UnClaimEvent> fireUnClaimEvent(@NotNull OnlineUser user, @NotNull TownClaim claim) {
        return fireEvent(new UnClaimEvent((BukkitUser) user, claim));
    }

    @Override
    public Optional<PlayerEnterTownEvent> firePlayerEnterTownEvent(@NotNull OnlineUser user, @NotNull TownClaim claim,
                                                                   @NotNull Position fromPosition, @NotNull Position toPosition) {
        return fireEvent(new PlayerEnterTownEvent((BukkitUser) user, claim, fromPosition, toPosition));
    }

    @Override
    public Optional<PlayerLeaveTownEvent> firePlayerLeaveTownEvent(@NotNull OnlineUser user, @NotNull TownClaim claim,
                                                                   @NotNull Position fromPosition, @NotNull Position toPosition) {
        return fireEvent(new PlayerLeaveTownEvent((BukkitUser) user, claim, fromPosition, toPosition));
    }

    @Override
    public Optional<TownCreateEvent> fireTownCreateEvent(@NotNull OnlineUser user, @NotNull String townName) {
        return fireEvent(new TownCreateEvent((BukkitUser) user, townName));
    }

    @Override
    public Optional<TownDisbandEvent> fireTownDisbandEvent(@NotNull OnlineUser user, @NotNull Town town) {
        return fireEvent(new TownDisbandEvent((BukkitUser) user, town));
    }
}