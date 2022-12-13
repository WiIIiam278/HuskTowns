package net.william278.husktowns.network;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

public abstract class Broker {

    protected final HuskTowns plugin;

    protected Broker(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
    }

    public abstract void initialize() throws RuntimeException;

    protected abstract void send(@NotNull Message message, @NotNull OnlineUser sender);

    public void onReceive(@NotNull OnlineUser receiver, @NotNull Message message) {

    }

    protected abstract void changeServer(@NotNull OnlineUser user, @NotNull String server);

    /**
     * Identifies types of message brokers
     */
    public enum Type {
        PLUGIN_MESSAGE("Plugin Messages"),
        REDIS("Redis");
        @NotNull
        public final String displayName;

        Type(@NotNull String displayName) {
            this.displayName = displayName;
        }
    }

}
