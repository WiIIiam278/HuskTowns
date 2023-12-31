/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.husktowns;

import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.roxeez.advancement.AdvancementManager;
import net.roxeez.advancement.display.BackgroundType;
import net.roxeez.advancement.display.FrameType;
import net.roxeez.advancement.trigger.TriggerType;
import net.william278.desertwell.util.Version;
import net.william278.husktowns.advancement.Advancement;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.command.BukkitCommand;
import net.william278.husktowns.config.*;
import net.william278.husktowns.database.Database;
import net.william278.husktowns.events.BukkitEventDispatcher;
import net.william278.husktowns.hook.*;
import net.william278.husktowns.listener.BukkitEventListener;
import net.william278.husktowns.listener.OperationHandler;
import net.william278.husktowns.manager.Manager;
import net.william278.husktowns.network.Broker;
import net.william278.husktowns.network.PluginMessageBroker;
import net.william278.husktowns.town.Invite;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
import net.william278.husktowns.user.User;
import net.william278.husktowns.util.BukkitTask;
import net.william278.husktowns.util.Validator;
import net.william278.husktowns.visualizer.Visualizer;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import space.arim.morepaperlib.MorePaperLib;
import space.arim.morepaperlib.commands.CommandRegistration;
import space.arim.morepaperlib.scheduling.GracefulScheduling;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

public class BukkitHuskTowns extends JavaPlugin implements HuskTowns, BukkitTask.Supplier,
        PluginMessageListener, BukkitEventDispatcher {

    private static BukkitHuskTowns instance;
    private BukkitAudiences audiences;
    private MorePaperLib paperLib;
    private Settings settings;
    private Locales locales;
    private Roles roles;
    private Presets presets;
    private Flags flags;
    private Levels levels;
    private Server server;
    private Database database;
    private Manager manager;
    @Nullable
    private Broker broker;
    private Validator validator;
    private OperationHandler operationHandler;
    private SpecialTypes specialTypes;
    private Advancement advancements;
    private Map<UUID, Deque<Invite>> invites = new HashMap<>();
    private Map<UUID, Preferences> userPreferences = new HashMap<>();
    private Map<UUID, Visualizer> visualizers = new HashMap<>();
    private ConcurrentLinkedQueue<Town> towns = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<String, ClaimWorld> claimWorlds = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<User>> globalUserList = new ConcurrentHashMap<>();
    private List<Hook> hooks = new ArrayList<>();
    private boolean loaded = false;

    @SuppressWarnings("unused")
    public BukkitHuskTowns() {
        super();
    }

    @TestOnly
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
        // Initialize PaperLib and Adventure
        this.paperLib = new MorePaperLib(this);
        this.audiences = BukkitAudiences.create(this);

        // Load configuration and subsystems
        this.loadConfig();
        this.operationHandler = new OperationHandler(this);
        this.validator = new Validator(this);
        this.invites = new HashMap<>();
        this.userPreferences = new HashMap<>();
        this.visualizers = new HashMap<>();
        this.hooks = new ArrayList<>();

        // Check for updates
        this.checkForUpdates();

        // Prepare the database and networking system
        this.database = this.loadDatabase();
        if (!database.hasLoaded()) {
            log(Level.SEVERE, "Failed to load database! Please check your credentials! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Load manager and broker
        this.manager = new Manager(this);
        this.broker = this.loadBroker();

        // Register hooks
        final PluginManager plugins = Bukkit.getPluginManager();
        if (settings.doEconomyHook()) {
            if (plugins.getPlugin("Vault") != null) {
                this.registerHook(new VaultEconomyHook(this));
            }
        }
        if (settings.doWebMapHook()) {
            if (plugins.getPlugin("BlueMap") != null) {
                this.registerHook(new BlueMapHook(this));
            } else if (plugins.getPlugin("dynmap") != null) {
                this.registerHook(new DynmapHook(this));
            } else if (plugins.getPlugin("Pl3xMap") != null) {
                this.registerHook(new Pl3xMapHook(this));
            }
        }
        if (settings.doLuckPermsHook() && plugins.getPlugin("LuckPerms") != null) {
            this.registerHook(new LuckPermsHook(this));
        }
        if (settings.doPlaceholderAPIHook() && plugins.getPlugin("PlaceholderAPI") != null) {
            this.registerHook(new PlaceholderAPIHook(this));
        }
        if (settings.doHuskHomesHook() && plugins.getPlugin("HuskHomes") != null) {
            this.registerHook(new HuskHomesHook(this));
        }
        if (settings.doPlanHook() && plugins.getPlugin("Plan") != null) {
            this.registerHook(new PlanHook(this));
        }

        // Load towns and claim worlds
        this.loadData();

        // Prepare commands
        this.registerCommands();

        // Register event listener
        Bukkit.getPluginManager().registerEvents(new BukkitEventListener(this), this);

        // Register metrics
        initializeMetrics();
        log(Level.INFO, "Enabled HuskTowns v" + getVersion());
    }

    @Override
    public void onDisable() {
        if (database != null) {
            getDatabase().close();
        }
        if (visualizers != null) {
            visualizers.values().forEach(Visualizer::cancel);
        }
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
    public Flags getFlags() {
        return flags;
    }

    @Override
    public void setFlags(@NotNull Flags flags) {
        this.flags = flags;
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
    public OperationHandler getOperationHandler() {
        return operationHandler;
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
    public ConcurrentLinkedQueue<Town> getTowns() {
        return towns;
    }

    @Override
    public void setTowns(@NotNull List<Town> towns) {
        this.towns = new ConcurrentLinkedQueue<>(towns);
    }

    @Override
    @NotNull
    public Map<String, ClaimWorld> getClaimWorlds() {
        return claimWorlds;
    }

    @Override
    public void setClaimWorlds(@NotNull Map<String, ClaimWorld> claimWorlds) {
        this.claimWorlds = new ConcurrentHashMap<>(claimWorlds);
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

    public void registerCommands() {
        getCommands().forEach(command -> new BukkitCommand(command, this).register());
    }

    @Override
    public double getHighestBlockAt(@NotNull Position position) {
        final org.bukkit.World world = Bukkit.getWorld(position.getWorld().getName()) == null
                ? Bukkit.getWorld(position.getWorld().getUuid())
                : Bukkit.getWorld(position.getWorld().getName());
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
    public double getHighestYAt(double x, double z, @NotNull World world) {
        final org.bukkit.World bukkitWorld = Bukkit.getWorld(world.getName()) == null
                ? Bukkit.getWorld(world.getUuid()) : Bukkit.getWorld(world.getName());
        if (bukkitWorld == null) {
            return 64D;
        }
        return bukkitWorld.getHighestBlockYAt((int) Math.floor(x), (int) Math.floor(z));
    }

    @Override
    @NotNull
    public List<Hook> getHooks() {
        return hooks;
    }

    @Override
    public void dispatchCommand(@NotNull String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        if (command.isBlank()) return;
        getServer().dispatchCommand(getServer().getConsoleSender(), command);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (broker != null && broker instanceof PluginMessageBroker pluginMessenger
                && getSettings().getBrokerType() == Broker.Type.PLUGIN_MESSAGE) {
            pluginMessenger.onReceive(channel, BukkitUser.adapt(player), message);
        }
    }

    @NotNull
    @Override
    public AudienceProvider getAudiences() {
        return audiences;
    }

    @NotNull
    public GracefulScheduling getScheduler() {
        return paperLib.scheduling();
    }

    @NotNull
    public CommandRegistration getCommandRegistrar() {
        return paperLib.commandRegistration();
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    private void initializeMetrics() {
        try {
            final Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);
            metrics.addCustomChart(new SimplePie("bungee_mode",
                    () -> settings.doCrossServer() ? "true" : "false"));
            metrics.addCustomChart(new SimplePie("language",
                    () -> settings.getLanguage().toLowerCase()));
            metrics.addCustomChart(new SimplePie("database_type",
                    () -> settings.getDatabaseType().name().toLowerCase()));
            metrics.addCustomChart(new SimplePie("using_economy",
                    () -> getEconomyHook().isPresent() ? "true" : "false"));
            metrics.addCustomChart(new SimplePie("using_map",
                    () -> getMapHook().isPresent() ? "true" : "false"));
            getMapHook().ifPresent(hook -> metrics.addCustomChart(new SimplePie("map_type",
                    () -> hook.getName().toLowerCase())));
            getMessageBroker().ifPresent(broker -> metrics.addCustomChart(new SimplePie("messenger_type",
                    () -> settings.getBrokerType().name().toLowerCase())));
        } catch (Exception e) {
            log(Level.WARNING, "Failed to initialize plugin metrics", e);
        }
    }

    @Override
    public void awardAdvancement(@NotNull Advancement advancement, @NotNull OnlineUser user) {
        if (paperLib.scheduling().isUsingFolia()) {
            return; // Advancements aren't supported yet by Folia
        }

        final NamespacedKey key = NamespacedKey.fromString(advancement.getKey(), this);
        if (key == null) {
            return;
        }

        final org.bukkit.advancement.Advancement bukkitAdvancement = Bukkit.getAdvancement(key);
        if (bukkitAdvancement == null) {
            return;
        }
        final AdvancementProgress progress = ((BukkitUser) user).getPlayer().getAdvancementProgress(bukkitAdvancement);
        if (progress.isDone()) {
            return;
        }
        getPlugin().runSync(() -> bukkitAdvancement.getCriteria().forEach(progress::awardCriteria));
    }

    @Override
    public Optional<Advancement> getAdvancements() {
        return Optional.ofNullable(advancements);
    }

    @Override
    public void setAdvancements(@NotNull Advancement advancements) {
        if (paperLib.scheduling().isUsingFolia()) {
            log(Level.WARNING, "Advancements are not currently supported on Paper servers using Folia");
            return;
        }
        this.advancements = advancements;

        this.runSync(() -> {
            final AdvancementManager manager = new AdvancementManager(this);
            registerAdvancement(advancements, manager, null);
            manager.createAll(true);
        });
    }

    private void registerAdvancement(@NotNull Advancement advancement, @NotNull AdvancementManager manager,
                                     @Nullable net.roxeez.advancement.Advancement parent) {
        if (paperLib.scheduling().isUsingFolia()) {
            return; // Advancements aren't supported yet by Folia
        }

        final NamespacedKey key = NamespacedKey.fromString(advancement.getKey(), this);
        if (key == null) {
            return;
        }

        final net.roxeez.advancement.Advancement bukkitAdvancement = new net.roxeez.advancement.Advancement(key);
        manager.register(((context) -> {
            bukkitAdvancement.setDisplay(display -> {
                display.setTitle(advancement.getTitle());
                display.setDescription(advancement.getDescription());
                display.setIcon(Optional.ofNullable(Material.matchMaterial(advancement.getIcon())).orElse(Material.STONE));
                display.setBackground(BackgroundType.GRANITE);
                display.setToast(advancement.doSendNotification());
                display.setAnnounce(advancement.doSendNotification());
                display.setFrame(switch (advancement.getFrame()) {
                    case TASK -> FrameType.TASK;
                    case CHALLENGE -> FrameType.CHALLENGE;
                    case GOAL -> FrameType.GOAL;
                });
            });
            if (parent != null) {
                bukkitAdvancement.setParent(parent.getKey());
            }
            bukkitAdvancement.addCriteria("husktowns_completed", TriggerType.IMPOSSIBLE, (impossible -> {
            }));
            return bukkitAdvancement;
        }));
        advancement.getChildren().forEach(child -> registerAdvancement(child, manager, bukkitAdvancement));
    }

    @NotNull
    @Override
    public Map<String, List<User>> getGlobalUserList() {
        return globalUserList;
    }

    @Override
    @NotNull
    public BukkitHuskTowns getPlugin() {
        return this;
    }

}