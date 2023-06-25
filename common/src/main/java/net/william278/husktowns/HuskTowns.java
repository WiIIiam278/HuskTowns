/*
 * This file is part of HuskTowns by William278. Do not redistribute!
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  All rights reserved.
 *
 *  This source code is provided as reference to licensed individuals that have purchased the HuskTowns
 *  plugin once from any of the official sources it is provided. The availability of this code does
 *  not grant you the rights to modify, re-distribute, compile or redistribute this source code or
 *  "plugin" outside this intended purpose. This license does not cover libraries developed by third
 *  parties that are utilised in the plugin.
 */

package net.william278.husktowns;

import net.kyori.adventure.key.Key;
import net.william278.annotaml.Annotaml;
import net.william278.desertwell.util.UpdateChecker;
import net.william278.desertwell.util.Version;
import net.william278.husktowns.advancement.AdvancementTracker;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.command.AdminTownCommand;
import net.william278.husktowns.command.Command;
import net.william278.husktowns.command.HuskTownsCommand;
import net.william278.husktowns.command.TownCommand;
import net.william278.husktowns.config.*;
import net.william278.husktowns.database.Database;
import net.william278.husktowns.database.MySqlDatabase;
import net.william278.husktowns.database.SqLiteDatabase;
import net.william278.husktowns.events.EventDispatcher;
import net.william278.husktowns.hook.EconomyHook;
import net.william278.husktowns.hook.Hook;
import net.william278.husktowns.hook.MapHook;
import net.william278.husktowns.hook.TeleportationHook;
import net.william278.husktowns.listener.OperationHandler;
import net.william278.husktowns.manager.Manager;
import net.william278.husktowns.network.Broker;
import net.william278.husktowns.network.PluginMessageBroker;
import net.william278.husktowns.network.RedisBroker;
import net.william278.husktowns.town.Invite;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.ConsoleUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
import net.william278.husktowns.user.User;
import net.william278.husktowns.util.DataPruner;
import net.william278.husktowns.util.GsonProvider;
import net.william278.husktowns.util.TaskRunner;
import net.william278.husktowns.util.Validator;
import net.william278.husktowns.visualizer.Visualizer;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.stream.Collectors;

public interface HuskTowns extends TaskRunner, EventDispatcher, AdvancementTracker, DataPruner, GsonProvider {

    int SPIGOT_RESOURCE_ID = 92672;
    int BSTATS_PLUGIN_ID = 11265;

    @NotNull
    Settings getSettings();

    void setSettings(@NotNull Settings settings);

    @NotNull
    Locales getLocales();

    void setLocales(@NotNull Locales locales);

    @NotNull
    Roles getRoles();

    void setRoles(@NotNull Roles roles);

    @NotNull
    Presets getRulePresets();

    void setRulePresets(@NotNull Presets presets);

    @NotNull
    Flags getFlags();

    void setFlags(@NotNull Flags flags);

    @NotNull
    Levels getLevels();

    void setLevels(@NotNull Levels levels);

    @NotNull
    String getServerName();

    void setServer(Server server);

    @NotNull
    Database getDatabase();

    @NotNull
    Manager getManager();

    @NotNull
    Optional<Broker> getMessageBroker();

    @NotNull
    Validator getValidator();

    @NotNull
    OperationHandler getOperationHandler();

    @NotNull
    SpecialTypes getSpecialTypes();

    void setSpecialTypes(@NotNull SpecialTypes specialTypes);

    @NotNull
    Map<UUID, Deque<Invite>> getInvites();

    default void addInvite(@NotNull UUID recipient, @NotNull Invite invite) {
        if (!getInvites().containsKey(recipient)) {
            getInvites().put(recipient, new ArrayDeque<>());
        }
        getInvites().get(recipient).add(invite);
    }

    default void removeInvite(@NotNull UUID recipient, @NotNull Invite invite) {
        if (getInvites().containsKey(recipient)) {
            getInvites().get(recipient).remove(invite);
            if (getInvites().get(recipient).isEmpty()) {
                getInvites().remove(recipient);
            }
        }
    }

    default Optional<Invite> getLastInvite(@NotNull User recipient, @Nullable String selectedInviter) {
        if (getInvites().containsKey(recipient.getUuid())) {
            Deque<Invite> invites = getInvites().get(recipient.getUuid());
            if (invites.isEmpty()) {
                return Optional.empty();
            }
            if (selectedInviter != null) {
                invites = invites.stream()
                        .filter(invite -> invite.getSender().getUsername().equalsIgnoreCase(selectedInviter))
                        .collect(Collectors.toCollection(ArrayDeque::new));
            }
            return Optional.of(invites.getLast());

        }
        return Optional.empty();
    }

    default void clearInvites(@NotNull UUID recipient) {
        getInvites().get(recipient).clear();
    }

    @NotNull
    Map<UUID, Preferences> getUserPreferences();

    default void setUserPreferences(@NotNull UUID uuid, @NotNull Preferences preferences) {
        getUserPreferences().put(uuid, preferences);
    }

    default Optional<Preferences> getUserPreferences(@NotNull UUID uuid) {
        return Optional.ofNullable(getUserPreferences().get(uuid));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isLoaded();

    void setLoaded(boolean loaded);

    @NotNull
    ConcurrentLinkedQueue<Town> getTowns();

    default void removeTown(@NotNull Town town) {
        getTowns().removeIf(t -> t.getId() == town.getId());
    }

    default void updateTown(@NotNull Town town) {
        removeTown(town);
        getTowns().add(town);
    }

    default Optional<Member> getUserTown(@NotNull User user) throws IllegalStateException {
        return getTowns().stream()
                .filter(town -> town.getMembers().containsKey(user.getUuid())).findFirst()
                .flatMap(town -> {
                    final int weight = town.getMembers().get(user.getUuid());
                    return Optional.of(getRoles().fromWeight(weight)
                            .map(role -> new Member(user, town, role))
                            .orElseThrow(() -> new IllegalStateException("No role found for weight \"" + weight + "\"")));
                });
    }

    @NotNull
    default Town getAdminTown() {
        return Town.admin(this);
    }

    void setTowns(@NotNull List<Town> towns);

    default void loadData() {
        final LocalTime startTime = LocalTime.now();
        log(Level.INFO, "Loading data...");
        runAsync(() -> {
            loadClaimWorlds();
            loadTowns();
            pruneInactiveTowns();
            pruneOrphanClaims();
            log(Level.INFO, "Loaded data in " + (ChronoUnit.MILLIS.between(startTime, LocalTime.now()) / 1000d) + " seconds");
            setLoaded(true);
            loadHooks();
        });
    }

    default void loadClaimWorlds() {
        log(Level.INFO, "Loading claims from the " + getSettings().getDatabaseType().getDisplayName() + " database...");
        LocalTime startTime = LocalTime.now();
        final Map<String, ClaimWorld> loadedWorlds = new HashMap<>();
        final Map<World, ClaimWorld> worlds = getDatabase().getClaimWorlds(getServerName());
        worlds.forEach((world, claimWorld) -> loadedWorlds.put(world.getName(), claimWorld));
        for (final World serverWorld : getWorlds()) {
            if (getSettings().isUnclaimableWorld(serverWorld)) {
                continue;
            }

            if (worlds.keySet().stream().map(World::getName).noneMatch(uuid -> uuid.equals(serverWorld.getName()))) {
                log(Level.INFO, "Creating a new claim world for " + serverWorld.getName() + " in the database...");
                loadedWorlds.put(serverWorld.getName(), getDatabase().createClaimWorld(serverWorld));
            }
        }
        setClaimWorlds(loadedWorlds);
        final Collection<ClaimWorld> claimWorlds = getClaimWorlds().values();
        final int claimCount = claimWorlds.stream().mapToInt(ClaimWorld::getClaimCount).sum();
        final int worldCount = claimWorlds.size();
        log(Level.INFO, "Loaded " + claimCount + " claim(s) across " + worldCount + " world(s) in " +
                        (ChronoUnit.MILLIS.between(startTime, LocalTime.now()) / 1000d) + " seconds");
    }

    default void loadTowns() {
        log(Level.INFO, "Loading towns from the database...");
        LocalTime startTime = LocalTime.now();
        setTowns(getDatabase().getAllTowns());

        final int townCount = getTowns().size();
        final int memberCount = getTowns().stream().mapToInt(town -> town.getMembers().size()).sum();
        log(Level.INFO, "Loaded " + townCount + " town(s) with " + memberCount + " member(s) in " +
                        (ChronoUnit.MILLIS.between(startTime, LocalTime.now()) / 1000d) + " seconds");
    }

    default Optional<Town> findTown(int id) {
        return getTowns().stream()
                .filter(town -> town.getId() == id)
                .findFirst();
    }

    default Optional<Town> findTown(@NotNull String name) {
        return getTowns().stream()
                .filter(town -> town.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @NotNull
    Map<String, ClaimWorld> getClaimWorlds();

    default Optional<TownClaim> getClaimAt(@NotNull Chunk chunk, @NotNull World world) {
        return Optional.ofNullable(getClaimWorlds().get(world.getName()))
                .flatMap(claimWorld -> claimWorld.getClaimAt(chunk, this));
    }

    default Optional<TownClaim> getClaimAt(@NotNull Position position) {
        return Optional.ofNullable(getClaimWorlds().get(position.getWorld().getName()))
                .flatMap(claimWorld -> claimWorld.getClaimAt(position.getChunk(), this));
    }

    default Optional<ClaimWorld> getClaimWorld(@NotNull World world) {
        return Optional.ofNullable(getClaimWorlds().get(world.getName()));
    }

    void setClaimWorlds(@NotNull Map<String, ClaimWorld> claimWorlds);

    @NotNull
    List<World> getWorlds();

    @NotNull
    Map<UUID, Visualizer> getVisualizers();

    default void highlightClaims(@NotNull OnlineUser user, @NotNull List<TownClaim> claim) {
        if (getVisualizers().containsKey(user.getUuid())) {
            getVisualizers().get(user.getUuid()).cancel();
        }
        // Display for 5 seconds
        final Visualizer visualizer = new Visualizer(user, claim, user.getWorld(), this);
        getVisualizers().put(user.getUuid(), visualizer);
        visualizer.show(5L * 20L);
    }

    default void highlightClaim(@NotNull OnlineUser user, @NotNull TownClaim claim) {
        highlightClaims(user, Collections.singletonList(claim));
    }

    File getDataFolder();

    InputStream getResource(@NotNull String name);

    void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... throwable);

    @NotNull
    ConsoleUser getConsole();

    default void loadConfig() throws RuntimeException {
        try {
            setSettings(Annotaml.create(new File(getDataFolder(), "config.yml"), Settings.class).get());
            setRoles(Annotaml.create(new File(getDataFolder(), "roles.yml"), Roles.class).get());
            setRulePresets(Annotaml.create(new File(getDataFolder(), "rules.yml"), Presets.class).get());
            setFlags(Annotaml.create(new File(getDataFolder(), "flags.yml"), Flags.class).get());
            setLevels(Annotaml.create(new File(getDataFolder(), "levels.yml"), new Levels()).get());
            setLocales(Annotaml.create(new File(getDataFolder(), "messages-" + getSettings().getLanguage() + ".yml"),
                    Annotaml.create(Locales.class, getResource("locales/" + getSettings().getLanguage() + ".yml")).get()).get());
            setSpecialTypes(Annotaml.create(SpecialTypes.class, getResource("data/special_types.yml")).get());
            if (getSettings().doCrossServer()) {
                setServer(Annotaml.create(new File(getDataFolder(), "server.yml"), Server.class).get());
            }
            if (getSettings().doAdvancements()) {
                loadAdvancements();
            }
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            log(Level.SEVERE, "Failed to load configuration files", e);
            throw new RuntimeException(e);
        }
    }

    default void reload() {
        setLoaded(false);
        loadConfig();
        loadData();
    }

    @NotNull
    default List<Command> getCommands() {
        return List.of(
                new HuskTownsCommand(this),
                new TownCommand(this),
                new AdminTownCommand(this)
        );
    }

    @NotNull
    default Database loadDatabase() throws RuntimeException {
        final Database database = switch (getSettings().getDatabaseType()) {
            case MYSQL -> new MySqlDatabase(this);
            case SQLITE -> new SqLiteDatabase(this);
        };
        database.initialize();
        log(Level.INFO, "Successfully initialized the " + getSettings().getDatabaseType().getDisplayName() + " database");
        return database;
    }

    @Nullable
    default Broker loadBroker() throws RuntimeException {
        if (!getSettings().doCrossServer()) {
            return null;
        }

        final Broker broker = switch (getSettings().getBrokerType()) {
            case PLUGIN_MESSAGE -> new PluginMessageBroker(this);
            case REDIS -> new RedisBroker(this);
        };
        broker.initialize();
        log(Level.INFO, "Successfully initialized the " + getSettings().getBrokerType().getDisplayName() + " broker");
        return broker;
    }

    double getHighestBlockAt(@NotNull Position position);

    void initializePluginChannels();

    @NotNull
    Version getVersion();

    @NotNull
    default UpdateChecker getUpdateChecker() {
        return UpdateChecker.builder()
                .currentVersion(getVersion())
                .resource(Integer.toString(SPIGOT_RESOURCE_ID))
                .endpoint(UpdateChecker.Endpoint.SPIGOT)
                .build();
    }

    default void checkForUpdates() {
        if (getSettings().doCheckForUpdates()) {
            getUpdateChecker().check().thenAccept(updated -> {
                if (updated.isUpToDate()) {
                    return;
                }
                log(Level.WARNING, "A new version of HuskTowns is available: v" + updated.getLatestVersion()
                                   + " (Running: v" + getVersion() + ")");
            });
        }
    }

    @NotNull
    List<? extends OnlineUser> getOnlineUsers();

    default Optional<? extends OnlineUser> findOnlineUser(@NotNull String username) {
        return getOnlineUsers().stream()
                .filter(online -> online.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    @NotNull
    List<Hook> getHooks();

    default void registerHook(@NotNull Hook hook) {
        getHooks().add(hook);
    }

    default void loadHooks() {
        getHooks().stream().filter(Hook::isDisabled).forEach(Hook::enable);
        log(Level.INFO, "Successfully loaded " + getHooks().size() + " hooks");
    }

    default <T extends Hook> Optional<T> getHook(@NotNull Class<T> hookClass) {
        return getHooks().stream()
                .filter(hook -> hookClass.isAssignableFrom(hook.getClass()))
                .map(hookClass::cast)
                .findFirst();
    }

    default Optional<EconomyHook> getEconomyHook() {
        return getHook(EconomyHook.class);
    }

    default Optional<MapHook> getMapHook() {
        return getHook(MapHook.class);
    }

    default Optional<TeleportationHook> getTeleportationHook() {
        return getHook(TeleportationHook.class);
    }

    void dispatchCommand(@NotNull String command);

    @NotNull
    default Key getKey(@NotNull String... data) {
        if (data.length == 0) {
            throw new IllegalArgumentException("Cannot create a key with no data");
        }
        @Subst("foo") final String joined = String.join("/", data);
        return Key.key("husktowns", joined);
    }

}