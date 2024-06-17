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

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.AudienceProvider;
import net.william278.desertwell.util.UpdateChecker;
import net.william278.desertwell.util.Version;
import net.william278.husktowns.advancement.AdvancementProvider;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.command.AdminTownCommand;
import net.william278.husktowns.command.Command;
import net.william278.husktowns.command.HuskTownsCommand;
import net.william278.husktowns.command.TownCommand;
import net.william278.husktowns.config.ConfigProvider;
import net.william278.husktowns.database.Database;
import net.william278.husktowns.database.MySqlDatabase;
import net.william278.husktowns.database.SqLiteDatabase;
import net.william278.husktowns.events.EventDispatcher;
import net.william278.husktowns.hook.EconomyHook;
import net.william278.husktowns.hook.HookManager;
import net.william278.husktowns.hook.MapHook;
import net.william278.husktowns.hook.TeleportationHook;
import net.william278.husktowns.listener.OperationHandler;
import net.william278.husktowns.listener.UserListener;
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
import net.william278.husktowns.util.*;
import net.william278.husktowns.visualizer.Visualizer;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

public interface HuskTowns extends Task.Supplier, ConfigProvider, EventDispatcher, UserListProvider,
    AdvancementProvider, DataPruner, GsonProvider, OperationHandler, UserListener {

    int SPIGOT_RESOURCE_ID = 92672;
    int BSTATS_PLUGIN_ID = 11265;

    @NotNull
    Database getDatabase();

    @NotNull
    Manager getManager();

    @NotNull
    Optional<Broker> getMessageBroker();

    @NotNull
    Validator getValidator();

    @NotNull
    Map<UUID, Deque<Invite>> getInvites();

    @NotNull
    HookManager getHookManager();

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

    default void editUserPreferences(@NotNull User user, @NotNull Consumer<Preferences> consumer) {
        final Preferences preferences = getUserPreferences(user.getUuid()).orElse(Preferences.getDefaults());
        runAsync(() -> {
            consumer.accept(preferences);
            setUserPreferences(user.getUuid(), preferences);
            getDatabase().updateUser(user, preferences);
        });
    }

    boolean isLoaded();

    void setLoaded(boolean loaded);

    @NotNull
    Set<Town> getTowns();

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
            try {
                loadClaimWorlds();
                loadTowns();
                pruneInactiveTowns();
                pruneOrphanClaims();
                pruneLocalTownWars();
                log(Level.INFO, String.format("Loaded data in %s seconds.",
                    (ChronoUnit.MILLIS.between(startTime, LocalTime.now()) / 1000d)));
                setLoaded(true);
            } catch (IllegalStateException e) {
                setLoaded(false);
                log(Level.SEVERE, String.format("Failed to load data (after %s seconds). Interaction will be disabled!",
                    (ChronoUnit.MILLIS.between(startTime, LocalTime.now()) / 1000d)), e);
            }
        });
    }

    default void loadClaimWorlds() throws IllegalStateException {
        log(Level.INFO, "Loading claims from the " + getSettings().getDatabase().getType().getDisplayName() + " database...");
        LocalTime startTime = LocalTime.now();
        final Map<String, ClaimWorld> loadedWorlds = new HashMap<>();
        final Map<World, ClaimWorld> worlds = getDatabase().getClaimWorlds(getServerName());
        worlds.forEach((world, claimWorld) -> loadedWorlds.put(world.getName(), claimWorld));
        for (final World serverWorld : getWorlds()) {
            if (getSettings().getGeneral().isUnclaimableWorld(serverWorld)) {
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

    default void loadTowns() throws IllegalStateException {
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

    default void highlightClaims(@NotNull OnlineUser user, @NotNull List<TownClaim> claim, final long duration) {
        // Display for 5 seconds
        this.stopHighlightingClaims(user);
        final Visualizer visualizer = new Visualizer(user, claim, user.getWorld(), this);
        getVisualizers().put(user.getUuid(), visualizer);
        visualizer.show(duration * 20L);
    }

    default void highlightClaims(@NotNull OnlineUser user, @NotNull List<TownClaim> claim) {
        this.highlightClaims(user, claim, 5L);
    }

    default void highlightClaim(@NotNull OnlineUser user, @NotNull TownClaim claim) {
        highlightClaims(user, Collections.singletonList(claim));
    }

    default void stopHighlightingClaims(@NotNull OnlineUser user) {
        if (getVisualizers().containsKey(user.getUuid())) {
            getVisualizers().get(user.getUuid()).cancel();
            getVisualizers().remove(user.getUuid());
        }
    }

    @NotNull
    File getDataFolder();

    InputStream getResource(@NotNull String name);

    @NotNull
    AudienceProvider getAudiences();

    @NotNull
    default Audience getAudience(@NotNull UUID user) {
        return getAudiences().player(user);
    }

    @NotNull
    default ConsoleUser getConsole() {
        return ConsoleUser.wrap(getAudiences().console());
    }

    void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... throwable);

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
        final Database.Type databaseType = getSettings().getDatabase().getType();
        final Database database = switch (databaseType) {
            case MYSQL, MARIADB -> new MySqlDatabase(this);
            case SQLITE -> new SqLiteDatabase(this);
        };
        database.initialize();
        log(Level.INFO, "Successfully initialized the " + databaseType.getDisplayName() + " database");
        return database;
    }

    @Nullable
    default Broker loadBroker() throws RuntimeException {
        if (!getSettings().getCrossServer().isEnabled()) {
            return null;
        }

        final Broker.Type brokerType = getSettings().getCrossServer().getBrokerType();
        final Broker broker = switch (brokerType) {
            case PLUGIN_MESSAGE -> new PluginMessageBroker(this);
            case REDIS -> new RedisBroker(this);
        };
        broker.initialize();
        log(Level.INFO, "Successfully initialized the " + brokerType.getDisplayName() + " broker");
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
        if (getSettings().isCheckForUpdates()) {
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

    default void teleportUser(@NotNull OnlineUser user, @NotNull Position position, @Nullable String server,
                              boolean instant) {
        final String targetServer = server != null ? server : getServerName();
        getTeleportationHook().ifPresentOrElse(
            hook -> hook.teleport(user, position, targetServer, instant),
            () -> {
                if (getSettings().getCrossServer().isEnabled() && !targetServer.equals(getServerName())) {
                    final Optional<Preferences> optionalPreferences = getUserPreferences(user.getUuid());
                    optionalPreferences.ifPresent(preferences -> runAsync(() -> {
                        preferences.setTeleportTarget(position);
                        getDatabase().updateUser(user, preferences);
                        getMessageBroker().ifPresent(broker -> broker.changeServer(user, targetServer));
                    }));
                    return;
                }

                runSync(() -> {
                    user.teleportTo(position);
                    getLocales().getLocale("teleportation_complete").ifPresent(
                        locale -> user.sendMessage(getSettings().getGeneral().getNotificationSlot(), locale)
                    );
                }, user);
            }
        );
    }

    double getHighestYAt(double x, double z, @NotNull World world);

    default Optional<EconomyHook> getEconomyHook() {
        return getHookManager().getHook(EconomyHook.class);
    }

    @NotNull
    default String formatMoney(@NotNull BigDecimal amount) {
        return getEconomyHook()
            .map(hook -> hook.formatMoney(amount))
            .orElse(getLocales().getRawLocale(
                "town_points_format", Integer.toString(amount.intValue())
            ).orElse(Integer.toString(amount.intValue())));
    }

    default Optional<MapHook> getMapHook() {
        return getHookManager().getHook(MapHook.class);
    }

    default Optional<TeleportationHook> getTeleportationHook() {
        return getHookManager().getHook(TeleportationHook.class);
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