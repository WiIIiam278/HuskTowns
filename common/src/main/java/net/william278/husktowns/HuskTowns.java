package net.william278.husktowns;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import net.kyori.adventure.key.Key;
import net.william278.annotaml.Annotaml;
import net.william278.desertwell.Version;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.command.Command;
import net.william278.husktowns.config.Locales;
import net.william278.husktowns.config.Roles;
import net.william278.husktowns.config.Server;
import net.william278.husktowns.config.Settings;
import net.william278.husktowns.database.Database;
import net.william278.husktowns.database.SqLiteDatabase;
import net.william278.husktowns.hook.EconomyHook;
import net.william278.husktowns.hook.Hook;
import net.william278.husktowns.network.Broker;
import net.william278.husktowns.network.PluginMessageBroker;
import net.william278.husktowns.network.RedisBroker;
import net.william278.husktowns.town.Invite;
import net.william278.husktowns.town.Manager;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.ConsoleUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.User;
import net.william278.husktowns.util.TaskRunner;
import net.william278.husktowns.util.Validator;
import net.william278.husktowns.visualizer.Visualizer;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalTime;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public interface HuskTowns extends TaskRunner {

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

    @NotNull
    List<Command> getCommands();

    boolean isLoaded();

    void setLoaded(boolean loaded);

    @NotNull
    List<Town> getTowns();

    default Optional<Member> getUserTown(@NotNull User user) {
        for (int i = 0; i < getTowns().size(); i++) {
            final Town town = getTowns().get(i);
            if (town.getMembers().containsKey(user.getUuid())) {
                final int weight = town.getMembers().get(user.getUuid());
                return getRoles().fromWeight(weight)
                        .map(role -> new Member(user, town, role));
            }
        }
        return Optional.empty();
    }

    void setTowns(@NotNull List<Town> towns);

    default void loadData() {
        final LocalTime startTime = LocalTime.now();
        log(Level.INFO, "Loading data...");
        runAsync(() -> {
            loadClaimWorlds();
            loadTowns();
            pruneClaimWorlds();
            log(Level.INFO, "Loaded data in " + LocalTime.now().minusNanos(startTime.toNanoOfDay()) + "!");
        });
    }

    default void loadClaimWorlds() {
        log(Level.INFO, "Loading claims from the database...");
        LocalTime startTime = LocalTime.now();
        final Map<UUID, ClaimWorld> loadedWorlds = new HashMap<>();
        final Map<World, ClaimWorld> worlds = getDatabase().getServerClaimWorlds(getServerName());
        worlds.forEach((world, claimWorld) -> loadedWorlds.put(world.getUuid(), claimWorld));
        for (final World serverWorld : getWorlds()) {
            if (worlds.keySet().stream().map(World::getUuid).noneMatch(uuid -> uuid.equals(serverWorld.getUuid()))) {
                log(Level.INFO, "Creating a new claim world for " + serverWorld.getName() + " in the database...");
                loadedWorlds.put(serverWorld.getUuid(), getDatabase().createClaimWorld(serverWorld));
            }
        }
        setClaimWorlds(loadedWorlds);
        final Collection<ClaimWorld> claimWorlds = getClaimWorlds().values();
        final int claimCount = claimWorlds.stream().mapToInt(ClaimWorld::getClaimCount).sum();
        final int worldCount = claimWorlds.size();
        final LocalTime claimLoadTime = LocalTime.now().minusNanos(startTime.toNanoOfDay());
        log(Level.INFO, "Loaded " + claimCount + " claim(s) across " + worldCount + " world(s) in " + claimLoadTime);
    }

    default void loadTowns() {
        log(Level.INFO, "Loading towns from the database...");
        LocalTime startTime = LocalTime.now();
        setTowns(getDatabase().getAllTowns());

        final int townCount = getTowns().size();
        final int memberCount = getTowns().stream().mapToInt(town -> town.getMembers().size()).sum();
        final LocalTime townLoadTime = LocalTime.now().minusNanos(startTime.toNanoOfDay());
        log(Level.INFO, "Loaded " + townCount + " town(s) with " + memberCount + " member(s) in " + townLoadTime);
    }

    default void pruneClaimWorlds() {
        log(Level.INFO, "Validating and pruning claims...");
        LocalTime startTime = LocalTime.now();
        getClaimWorlds().values().forEach(world -> {
            world.getClaims().keySet().removeIf(claim -> getTowns().stream().noneMatch(town -> town.getId() == claim));
            getDatabase().updateClaimWorld(world);
        });
        final LocalTime pruneTime = LocalTime.now().minusNanos(startTime.toNanoOfDay());
        log(Level.INFO, "Successfully validated and pruned claims in " + pruneTime);
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
    Map<UUID, ClaimWorld> getClaimWorlds();

    default Optional<TownClaim> getClaimAt(@NotNull Chunk chunk, @NotNull World world) {
        return Optional.ofNullable(getClaimWorlds().get(world.getUuid()))
                .flatMap(claimWorld -> claimWorld.getClaimAt(chunk, this));
    }

    default Optional<TownClaim> getClaimAt(@NotNull Position position) {
        return Optional.ofNullable(getClaimWorlds().get(position.getWorld().getUuid()))
                .flatMap(claimWorld -> claimWorld.getClaimAt(position.getChunk(), this));
    }

    default Optional<ClaimWorld> getClaimWorld(@NotNull World world) {
        return Optional.ofNullable(getClaimWorlds().get(world.getUuid()));
    }

    void setClaimWorlds(@NotNull Map<UUID, ClaimWorld> claimWorlds);

    @NotNull
    List<World> getWorlds();

    @NotNull
    Map<UUID, Visualizer> getVisualizers();

    default void highlightClaim(@NotNull OnlineUser user, @NotNull TownClaim claim) {
        if (getVisualizers().containsKey(user.getUuid())) {
            getVisualizers().get(user.getUuid()).cancel();
        }
        // Display for 5 seconds
        final Visualizer visualizer = new Visualizer(user, claim, user.getWorld(), this);
        getVisualizers().put(user.getUuid(), visualizer);
        visualizer.show(5L * 20L);
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
            setLocales(Annotaml.create(new File(getDataFolder(), "messages-" + getSettings().language + ".yml"),
                    Annotaml.create(Locales.class, getResource("locales/" + getSettings().language + ".yml")).get()).get());
            if (getSettings().crossServer) {
                setServer(Annotaml.create(new File(getDataFolder(), "server.yml"), Server.class).get());
            }
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Exception loading system configuration", e);
        }
    }

    @NotNull
    default Database loadDatabase() throws RuntimeException {
        final Database database = switch (getSettings().databaseType) {
            case MYSQL -> throw new RuntimeException("MySQL database support is not yet implemented");
            case SQLITE -> new SqLiteDatabase(this);
        };
        database.initialize();
        return database;
    }

    @Nullable
    default Broker loadBroker() throws RuntimeException {
        if (!getSettings().crossServer) {
            return null;
        }

        final Broker broker = switch (getSettings().brokerType) {
            case PLUGIN_MESSAGE -> new PluginMessageBroker(this);
            case REDIS -> new RedisBroker(this);
        };
        broker.initialize();
        return broker;
    }

    double getHighestBlockAt(@NotNull Position position);

    void initializePluginChannels();

    @NotNull
    Version getVersion();

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

    default Optional<EconomyHook> getEconomyHook() {
        return getHooks().stream()
                .filter(hook -> hook instanceof EconomyHook)
                .map(hook -> (EconomyHook) hook)
                .findFirst();
    }

    @NotNull
    default Key getKey(@NotNull String... data) {
        if (data.length == 0) {
            throw new IllegalArgumentException("Cannot create a key with no data");
        }
        @Subst("foo") final String joined = String.join("/", data);
        return Key.key("husktowns", joined);
    }

    @NotNull
    default Gson getGson() {
        return Converters.registerOffsetDateTime(new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                        .registerTypeAdapter(Color.class, (JsonDeserializer<Color>) (json, type, context) -> Color.decode(json.toString())))
                .create();
    }

}