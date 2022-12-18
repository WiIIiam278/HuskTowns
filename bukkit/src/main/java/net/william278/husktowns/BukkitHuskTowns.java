package net.william278.husktowns;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.config.Locales;
import net.william278.husktowns.config.Roles;
import net.william278.husktowns.config.Server;
import net.william278.husktowns.config.Settings;
import net.william278.husktowns.database.Database;
import net.william278.husktowns.listener.BukkitEventListener;
import net.william278.husktowns.network.Broker;
import net.william278.husktowns.network.PluginMessageBroker;
import net.william278.husktowns.town.Manager;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.util.Validator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class BukkitHuskTowns extends JavaPlugin implements HuskTowns, PluginMessageListener {

    private static BukkitHuskTowns instance;

    @NotNull
    public static BukkitHuskTowns getInstance() {
        return instance;
    }

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
    private List<Town> towns;
    private Map<UUID, ClaimWorld> claimWorlds;

    @Override
    public void onLoad() {
        // Set the instance
        audiences = BukkitAudiences.create(this);
    }

    @Override
    public void onEnable() {
        // Enable HuskTowns and load configuration
        this.loadConfig();
        this.validator = new Validator(this);

        // Prepare the database and networking system
        this.database = this.loadDatabase();
        this.manager = new Manager(this);
        this.broker = this.loadBroker();

        // Load towns and claim worlds
        this.loadClaims();
        this.loadTowns();

        // Register event listener
        Bukkit.getPluginManager().registerEvents(new BukkitEventListener(this), this);
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
    public void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... throwable) {
        if (throwable.length > 0) {
            getLogger().log(level, message, throwable[0]);
            return;
        }
        getLogger().log(level, message);
    }

    @Override
    public void initializePluginChannels() {
        Bukkit.getMessenger().registerIncomingPluginChannel(this, PluginMessageBroker.PLUGIN_CHANNEL_ID, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, PluginMessageBroker.PLUGIN_CHANNEL_ID);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (getSettings().brokerType != Broker.Type.PLUGIN_MESSAGE) {
            return;
        }
        //todo ((PluginMessageBroker) broker).onReceive(channel, player, message);
    }

    @NotNull
    public BukkitAudiences getAudiences() {
        return audiences;
    }
}