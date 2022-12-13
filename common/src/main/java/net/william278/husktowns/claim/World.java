package net.william278.husktowns.claim;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class World {

    private UUID uuid;

    private String name;

    private String environment;

    private World(@NotNull UUID uuid, @NotNull String name, @NotNull String environment) {
        this.uuid = uuid;
        this.name = name;
        this.environment = environment;
    }

    @SuppressWarnings("unused")
    private World() {
    }

    @NotNull
    public static World of(@NotNull UUID uuid, @NotNull String name, @NotNull String dimension) {
        return new World(uuid, name, dimension);
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getEnvironment() {
        return environment;
    }

}
