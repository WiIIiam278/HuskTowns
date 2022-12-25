package net.william278.husktowns;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.william278.desertwell.Version;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.command.BukkitCommand;
import net.william278.husktowns.command.Command;
import net.william278.husktowns.command.HuskTownsCommand;
import net.william278.husktowns.command.TownCommand;
import net.william278.husktowns.config.Locales;
import net.william278.husktowns.config.Roles;
import net.william278.husktowns.config.Server;
import net.william278.husktowns.config.Settings;
import net.william278.husktowns.database.Database;
import net.william278.husktowns.listener.BukkitEventListener;
import net.william278.husktowns.network.Broker;
import net.william278.husktowns.network.PluginMessageBroker;
import net.william278.husktowns.town.Invite;
import net.william278.husktowns.town.Manager;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.ConsoleUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.util.Validator;
import net.william278.husktowns.visualizer.Visualizer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
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

    @NotNull
    public static BukkitHuskTowns getInstance() {
        return instance;
    }

    private boolean loaded = false;
    private BukkitAudiences audiences;
    private Settings settings;
    private Locales locales;
    private Roles roles;
    private Server server;
    private Database database;
    private Manager manager;
    @Nullable
    private Broker broker;
    private Validator validator;
    private Map<UUID, Deque<Invite>> invites;
    private Map<UUID, Visualizer> visualizers;
    private List<Town> towns;
    private Map<UUID, ClaimWorld> claimWorlds;
    private List<Command> commands;

    @SuppressWarnings("unused")
    public BukkitHuskTowns() {
        super();
    }

    @SuppressWarnings("unused")
    private BukkitHuskTowns(@NotNull JavaPluginLoader loader, @NotNull PluginDescriptionFile description,
                            @NotNull File dataFolder, @NotNull File file) {
        super(loader, description, dataFolder, file);
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
        this.visualizers = new HashMap<>();

        // Prepare the database and networking system
        this.database = this.loadDatabase();
        this.manager = new Manager(this);
        this.broker = this.loadBroker();

        // Load towns and claim worlds
        this.loadData();

        // Prepare commands
        this.commands = List.of(new HuskTownsCommand(this), new TownCommand(this));
        this.commands.forEach(command -> new BukkitCommand(command, this).register());

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
    public Map<UUID, Deque<Invite>> getInvites() {
        return invites;
    }

    @Override
    @NotNull
    public List<Command> getCommands() {
        return commands;
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
        return world.getHighestBlockYAt((int) position.getX(), (int) position.getZ());
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
    public List<? extends OnlineUser> getOnlineUsers() {
        return Bukkit.getOnlinePlayers().stream().map(BukkitUser::adapt).toList();
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

}