package net.william278.husktowns.network;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

public abstract class Broker {

    protected final HuskTowns plugin;

    protected Broker(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
    }

    protected void handle(@NotNull OnlineUser receiver, @NotNull Message message) {
        switch (message.getType()) {
            case TOWN_DELETE -> message.getPayload().getUuid().ifPresent(uuid -> {
                //todo Delete town w/ UUID on this server
            });
            case TOWN_UPDATE -> message.getPayload().getUuid().ifPresent(uuid -> {
                //todo Pull new town data from DB w/ UUID on this server
            });
        }
    }

    public abstract void initialize() throws RuntimeException;

    protected abstract void send(@NotNull Message message, @NotNull OnlineUser sender);

    protected abstract void changeServer(@NotNull OnlineUser user, @NotNull String server);

    public abstract void close();

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
