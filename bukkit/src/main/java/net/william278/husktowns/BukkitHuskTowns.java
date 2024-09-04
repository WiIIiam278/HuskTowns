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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.roxeez.advancement.AdvancementManager;
import net.roxeez.advancement.display.BackgroundType;
import net.roxeez.advancement.display.FrameType;
import net.roxeez.advancement.trigger.TriggerType;
import net.william278.desertwell.util.Version;
import net.william278.husktowns.advancement.Advancement;
import net.william278.husktowns.api.BukkitHuskTownsAPI;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.command.BukkitCommand;
import net.william278.husktowns.config.*;
import net.william278.husktowns.database.Database;
import net.william278.husktowns.events.BukkitEventDispatcher;
import net.william278.husktowns.hook.*;
import net.william278.husktowns.hook.map.BlueMapHook;
import net.william278.husktowns.hook.map.DynmapHook;
import net.william278.husktowns.hook.map.Pl3xMapHook;
import net.william278.husktowns.listener.BukkitListener;
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
import space.arim.morepaperlib.scheduling.AsynchronousScheduler;
import space.arim.morepaperlib.scheduling.AttachedScheduler;
import space.arim.morepaperlib.scheduling.GracefulScheduling;
import space.arim.morepaperlib.scheduling.RegionalScheduler;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

@NoArgsConstructor
@Getter
public class BukkitHuskTowns extends JavaPlugin implements HuskTowns, BukkitTask.Supplier,
    PluginMessageListener, BukkitEventDispatcher {


    private AudienceProvider audiences;
    private MorePaperLib paperLib;
    private AsynchronousScheduler asyncScheduler;
    private RegionalScheduler regionalScheduler;
    private final Set<Town> towns = Sets.newConcurrentHashSet();
    private final Map<String, ClaimWorld> claimWorlds = Maps.newConcurrentMap();
    private final Map<UUID, Deque<Invite>> invites = Maps.newConcurrentMap();
    private final Map<UUID, Preferences> userPreferences = Maps.newConcurrentMap();
    private final Map<UUID, Visualizer> visualizers = Maps.newConcurrentMap();
    private final Map<String, List<User>> globalUserList = Maps.newConcurrentMap();
    private final Validator validator = new Validator(this);
    @Setter
    private boolean loaded = false;
    @Setter
    private Manager manager;
    @Setter
    private Set<Hook> hooks = Sets.newHashSet();
    @Setter
    private Settings settings;
    @Setter
    private Locales locales;
    @Setter
    private Roles roles;
    @Setter
    private RulePresets rulePresets;
    @Setter
    private Flags flags;
    @Setter
    private Levels levels;
    @Setter
    private Database database;
    @Nullable
    @Getter(AccessLevel.NONE)
    @Setter
    private Broker broker;
    @Setter
    @Getter(AccessLevel.NONE)
    private Server server;
    @Nullable
    @Getter(AccessLevel.NONE)
    private Advancement advancements;
    @Setter
    private HookManager hookManager;

    @TestOnly
    @SuppressWarnings("unused")
    private BukkitHuskTowns(@NotNull JavaPluginLoader loader, @NotNull PluginDescriptionFile description,
                            @NotNull File dataFolder, @NotNull File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onLoad() {
        // Load configuration and subsystems
        this.loadConfig();

        // Register hooks
        this.hookManager = new BukkitHookManager(this);
        final PluginManager plugins = Bukkit.getPluginManager();
        if (settings.getGeneral().isEconomyHook()) {
            if (plugins.getPlugin("Vault") != null) {
                hookManager.registerHook(new VaultEconomyHook(this));
            }
        }
        if (settings.getGeneral().getWebMapHook().isEnabled()) {
            if (plugins.getPlugin("BlueMap") != null) {
                hookManager.registerHook(new BlueMapHook(this));
            } else if (plugins.getPlugin("dynmap") != null) {
                hookManager.registerHook(new DynmapHook(this));
            } else if (plugins.getPlugin("Pl3xMap") != null) {
                hookManager.registerHook(new Pl3xMapHook(this));
            }
        }
        if (settings.getGeneral().isLuckpermsContextsHook() && plugins.getPlugin("LuckPerms") != null) {
            hookManager.registerHook(new LuckPermsHook(this));
        }
        if (settings.getGeneral().isPlaceholderapiHook() && plugins.getPlugin("PlaceholderAPI") != null) {
            hookManager.registerHook(new PlaceholderAPIHook(this));
        }
        if (settings.getGeneral().isHuskhomesHook() && plugins.getPlugin("HuskHomes") != null) {
            hookManager.registerHook(new HuskHomesHook(this));
        }
        if (settings.getGeneral().isPlanHook() && plugins.getPlugin("Plan") != null) {
            hookManager.registerHook(new PlanHook(this));
        }
        if (settings.getGeneral().isWorldGuardHook() && plugins.getPlugin("WorldGuard") != null) {
            hookManager.registerHook(new BukkitWorldGuardHook(this));
        }

        hookManager.registerOnLoad();
    }

    @Override
    public void onEnable() {
        // Initialize PaperLib and Adventure
        this.paperLib = new MorePaperLib(this);
        this.audiences = BukkitAudiences.create(this);

        // Load advancements
        if (this.settings.getGeneral().isDoAdvancements()) {
            loadAdvancements();
        }

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
        hookManager.registerOnEnable();

        // Load towns and claim worlds
        this.loadData();

        // Prepare commands
        this.registerCommands();

        // Register event listener
        new BukkitListener(this).register();

        // Register API
        BukkitHuskTownsAPI.register(this);

        // Register metrics
        initializeMetrics();
        log(Level.INFO, "Enabled HuskTowns v" + getVersion());
        checkForUpdates();
    }

    @Override
    public void onDisable() {
        if (database != null) {
            getDatabase().close();
        }
        visualizers.values().forEach(Visualizer::cancel);
        getMessageBroker().ifPresent(Broker::close);
        log(Level.INFO, "Disabled HuskTowns v" + getVersion());
    }

    @Override
    @NotNull
    public String getServerName() {
        return server != null ? server.getName() : "server";
    }

    public void setServerName(@NotNull Server server) {
        this.server = server;
    }

    @Override
    @NotNull
    public Path getConfigDirectory() {
        return getDataFolder().toPath();
    }

    @Override
    @NotNull
    public Optional<Broker> getMessageBroker() {
        return Optional.ofNullable(broker);
    }

    @Override
    public @NotNull HookManager getHookManager() {
        return hookManager;
    }

    @Override
    @NotNull
    public List<World> getWorlds() {
        return Bukkit.getWorlds().stream()
            .map(world -> World.of(
                world.getUID(), world.getName(),
                world.getEnvironment().name().toLowerCase())
            ).toList();
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
        getServer().getMessenger().registerIncomingPluginChannel(this, PluginMessageBroker.BUNGEE_CHANNEL_ID, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, PluginMessageBroker.BUNGEE_CHANNEL_ID);
    }

    @Override
    @NotNull
    public Version getVersion() {
        return Version.fromString(getDescription().getVersion(), "-");
    }

    @Override
    @NotNull
    public List<? extends OnlineUser> getOnlineUsers() {
        return Bukkit.getOnlinePlayers().stream()
            .map(p -> BukkitUser.adapt(p, this))
            .toList();
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
            && getSettings().getCrossServer().getBrokerType() == Broker.Type.PLUGIN_MESSAGE) {
            pluginMessenger.onReceive(channel, BukkitUser.adapt(player, this), message);
        }
    }

    @NotNull
    public CommandRegistration getCommandRegistrar() {
        return paperLib.commandRegistration();
    }

    @NotNull
    public GracefulScheduling getScheduler() {
        return paperLib.scheduling();
    }

    @NotNull
    public AsynchronousScheduler getAsyncScheduler() {
        return asyncScheduler == null
            ? asyncScheduler = getScheduler().asyncScheduler() : asyncScheduler;
    }

    @NotNull
    public RegionalScheduler getSyncScheduler() {
        return regionalScheduler == null
            ? regionalScheduler = getScheduler().globalRegionalScheduler() : regionalScheduler;
    }

    @NotNull
    public AttachedScheduler getUserSyncScheduler(@NotNull OnlineUser user) {
        return getScheduler().entitySpecificScheduler(((BukkitUser) user).getPlayer());
    }

    @Override
    public void setTowns(@NotNull List<Town> towns) {
        this.towns.clear();
        this.towns.addAll(towns);
    }

    @Override
    public void setClaimWorlds(@NotNull Map<String, ClaimWorld> claimWorlds) {
        this.claimWorlds.clear();
        this.claimWorlds.putAll(claimWorlds);
    }

    private void initializeMetrics() {
        try {
            final Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);
            metrics.addCustomChart(new SimplePie("bungee_mode",
                () -> settings.getCrossServer().isEnabled() ? "true" : "false"));
            metrics.addCustomChart(new SimplePie("language",
                () -> settings.getLanguage().toLowerCase()));
            metrics.addCustomChart(new SimplePie("database_type",
                () -> settings.getDatabase().getType().name().toLowerCase()));
            metrics.addCustomChart(new SimplePie("using_economy",
                () -> getEconomyHook().isPresent() ? "true" : "false"));
            metrics.addCustomChart(new SimplePie("using_map",
                () -> getMapHook().isPresent() ? "true" : "false"));
            getMapHook().ifPresent(hook -> metrics.addCustomChart(new SimplePie("map_type",
                () -> hook.getHookInfo().id().toLowerCase())));
            getMessageBroker().ifPresent(broker -> metrics.addCustomChart(new SimplePie("messenger_type",
                () -> settings.getCrossServer().getBrokerType().name().toLowerCase())));
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
        getPlugin().runSync(() -> bukkitAdvancement.getCriteria().forEach(progress::awardCriteria), user);
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

    @Override
    @NotNull
    public BukkitHuskTowns getPlugin() {
        return this;
    }

}