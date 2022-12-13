package net.william278.husktowns;

import net.william278.annotaml.Annotaml;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.config.Locales;
import net.william278.husktowns.config.Roles;
import net.william278.husktowns.config.Settings;
import net.william278.husktowns.database.Database;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public interface HuskTowns {

    boolean isLoaded();

    void setLoaded(boolean loaded);

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
    Database getDatabase();

    @NotNull
    List<Town> getTowns();

    void setTowns(@NotNull List<Town> towns);

    void saveTown(@NotNull Town town);

    default Optional<Town> findTown(@NotNull UUID uuid) {
        return getTowns().stream().filter(town -> town.getUuid().equals(uuid)).findFirst();
    }

    default Optional<Town> findTown(@NotNull String name) {
        return getTowns().stream().filter(town -> town.getName().equalsIgnoreCase(name)).findFirst();
    }

    @NotNull
    Map<UUID, ClaimWorld> getClaimWorlds();

    void setClaimWorlds(@NotNull Map<UUID, ClaimWorld> claimWorlds);

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
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Exception loading system configuration", e);
        }
    }

    @NotNull
    default Database loadDatabase() throws RuntimeException {
        final Database database = switch (getSettings().databaseType) {
            case MYSQL -> null;
            case SQLITE -> null;
        };
        database.initialize();
        return database;
    }


}