package net.william278.husktowns.listener;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class EventListener {

    protected final HuskTowns plugin;

    public EventListener(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
    }

    protected void onPlayerJoin(@NotNull OnlineUser user) {
        CompletableFuture.runAsync(() -> {
            final Optional<User> userData = plugin.getDatabase().getUser(user.getUuid());
            if (userData.isEmpty()) {
                plugin.getDatabase().createUser(user);
                return;
            }

            // Update the user's name if it has changed
            if (!userData.get().getUsername().equals(user.getUsername())) {
                plugin.getDatabase().updateUser(user);
            }
        }).exceptionally(e -> {
            plugin.log(Level.SEVERE, "Error loading user data on join", e);
            return null;
        });
    }

    protected void onPlayerQuit(@NotNull OnlineUser user) {
    }

}
