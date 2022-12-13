package net.william278.husktowns;

import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.config.Locales;
import net.william278.husktowns.config.Roles;
import net.william278.husktowns.config.Settings;
import net.william278.husktowns.database.Database;
import net.william278.husktowns.town.Town;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BukkitHuskTowns extends JavaPlugin implements HuskTowns {

    // Instance of the plugin
    private static BukkitHuskTowns instance;

    public static BukkitHuskTowns getInstance() {
        return instance;
    }

    private boolean loaded = false;
    private Settings settings;
    private Locales locales;
    private Roles roles;
    private Database database;
    private List<Town> towns;
    private Map<UUID, ClaimWorld> claimWorlds;

    @Override
    public void onLoad() {
        // Set the instance
        instance = this;
    }

    @Override
    public void onEnable() {
        // Enable HuskTowns
        this.loadConfig();

        this.database = this.loadDatabase();


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
    public Database getDatabase() {
        return database;
    }

    @Override
    @NotNull
    public List<Town> getTowns() {
        return towns;
    }

    @Override
    public void setTowns(@NotNull List<Town> towns) {

    }

    @Override
    @NotNull
    public Map<UUID, ClaimWorld> getClaimWorlds() {
        return claimWorlds;
    }

    @Override
    public void setClaimWorlds(@NotNull Map<UUID, ClaimWorld> claimWorlds) {

    }

    @Override
    @NotNull
    public List<World> getWorlds() {
        return Bukkit.getWorlds().stream()
                .map(world -> World.of(world.getUID(), world.getName(), world.getEnvironment().name().toLowerCase()))
                .toList();
    }
}