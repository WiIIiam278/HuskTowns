package net.william278.husktowns.user;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class User {

    private UUID uuid;
    private String username;

    protected User(@NotNull UUID uuid, @NotNull String username) {
        this.uuid = uuid;
        this.username = username;
    }

    @NotNull
    public static User of(@NotNull UUID uuid, @NotNull String username) {
        return new User(uuid, username);
    }

    @SuppressWarnings("unused")
    private User() {
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    @NotNull
    public String getUsername() {
        return username;
    }
}
