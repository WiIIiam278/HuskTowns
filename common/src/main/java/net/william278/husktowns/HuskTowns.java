package net.william278.husktowns;

import com.google.gson.*;
import net.william278.annotaml.Annotaml;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.config.Locales;
import net.william278.husktowns.config.Roles;
import net.william278.husktowns.config.Server;
import net.william278.husktowns.config.Settings;
import net.william278.husktowns.database.Database;
import net.william278.husktowns.database.SqLiteDatabase;
import net.william278.husktowns.network.Broker;
import net.william278.husktowns.network.PluginMessageBroker;
import net.william278.husktowns.network.RedisBroker;
import net.william278.husktowns.town.Manager;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.util.Validator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.OpenOption;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public interface HuskTowns {

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
    List<Town> getTowns();

    void setTowns(@NotNull List<Town> towns);

    default void loadTowns() {
        log(Level.INFO, "Loading towns from the database...");
        final LocalTime startTime = LocalTime.now();
        CompletableFuture.runAsync(() -> {
            final List<Town> towns = getDatabase().getAllTowns();
            this.setTowns(towns);
        }).thenRun(() -> {
            final int townCount = getTowns().size();
            final int memberCount = getTowns().stream().mapToInt(town -> town.getMembers().size()).sum();
            final LocalTime duration = LocalTime.now().minusNanos(startTime.toNanoOfDay());
            log(Level.INFO, "Loaded " + townCount + " with " + memberCount + " members in " + duration);
        });
    }

    default Optional<Town> findTown(long id) {
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

    void setClaimWorlds(@NotNull Map<UUID, ClaimWorld> claimWorlds);

    default void loadClaims() {
        log(Level.INFO, "Loading claims from the database...");
        final LocalTime startTime = LocalTime.now();
        CompletableFuture.runAsync(() -> {
            final Map<UUID, ClaimWorld> loadedWorlds = new HashMap<>();
            final Map<World, ClaimWorld> worlds = getDatabase().getServerClaimWorlds(getServerName());
            worlds.forEach((world, claimWorld) -> loadedWorlds.put(world.getUuid(), claimWorld));
            this.setClaimWorlds(loadedWorlds);
        }).thenRun(() -> {
            final Collection<ClaimWorld> claimWorlds = getClaimWorlds().values();
            final int claimCount = claimWorlds.stream().mapToInt(ClaimWorld::getClaimCount).sum();
            final int worldCount = claimWorlds.size();
            final LocalTime duration = LocalTime.now().minusNanos(startTime.toNanoOfDay());
            log(Level.INFO, "Loaded " + claimCount + " claims across " + worldCount + " worlds in " + duration);
        });
    }

    @NotNull
    List<World> getWorlds();

    File getDataFolder();

    InputStream getResource(@NotNull String name);

    void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... throwable);

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

    void initializePluginChannels();

    @NotNull
    default Gson getGson() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Color.class, (JsonDeserializer<Color>) (json, type, context) -> Color.decode(json.getAsString()))
                .create();
    }


}