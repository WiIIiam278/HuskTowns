package net.william278.husktowns.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

public class Message {

    public static final String TARGET_ALL = "ALL";
    @NotNull
    @Expose
    private Type type;

    @NotNull
    @Expose
    @SerializedName("target_type")
    private TargetType targetType;
    @NotNull
    @Expose
    private String target;
    @NotNull
    @Expose
    private Payload payload;
    @NotNull
    @Expose
    private String sender;

    @NotNull
    @Expose
    private String sourceServer;

    private Message(@NotNull Type type, @NotNull String target, @NotNull TargetType targetType, @NotNull Payload payload) {
        this.type = type;
        this.target = target;
        this.payload = payload;
        this.targetType = targetType;
    }

    @SuppressWarnings("unused")
    private Message() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public void send(@NotNull Broker broker, @NotNull OnlineUser sender) {
        this.sender = sender.getUsername();
        this.sourceServer = broker.getServer();
        broker.send(this, sender);
    }

    @NotNull
    public Type getType() {
        return type;
    }

    @NotNull
    public String getTarget() {
        return target;
    }

    @NotNull
    public TargetType getTargetType() {
        return targetType;
    }

    @NotNull
    public Payload getPayload() {
        return payload;
    }

    @NotNull
    public String getSender() {
        return sender;
    }

    @NotNull
    public String getSourceServer() {
        return sourceServer;
    }

    public static class Builder {
        private Type type;
        private Payload payload = Payload.empty();
        private TargetType targetType = TargetType.PLAYER;
        private String target;

        private Builder() {
        }

        @NotNull
        public Builder type(@NotNull Type type) {
            this.type = type;
            return this;
        }

        @NotNull
        public Builder payload(@NotNull Payload payload) {
            this.payload = payload;
            return this;
        }

        @NotNull
        public Builder target(@NotNull String username) {
            this.target = username;
            return this;
        }

        @NotNull
        public Builder target(@NotNull String target, @NotNull TargetType targetType) {
            this.target = target;
            this.targetType = targetType;
            return this;
        }

        @NotNull
        public Message build() {
            return new Message(type, target, targetType, payload);
        }

    }

    public enum Type {
        /**
         * Payload contains the ID of a town.
         * Indicates the target server should pull and cache new town data from the database for that town.
         */
        TOWN_UPDATE,
        /**
         * Payload contains the ID of a town.
         * Indicates the target server should remove all claims for that town and remove the town from memory.
         */
        TOWN_DELETE,
        TOWN_INVITE_REQUEST,
        TOWN_INVITE_REPLY,
        TOWN_LEVEL_UP,
        TOWN_TRANSFERRED,
        TOWN_EVICTED,
        TOWN_PROMOTED,
        TOWN_DEMOTED,
        TOWN_RENAMED,
        TOWN_CHAT_MESSAGE
    }

    public enum TargetType {
        /**
         * The target is a server name, or "all" to indicate all servers.
         */
        SERVER("Forward"),
        /**
         * The target is a player name, or "all" to indicate all players.
         */
        PLAYER("ForwardToPlayer");

        private final String pluginMessageChannel;

        TargetType(@NotNull String pluginMessageChannel) {
            this.pluginMessageChannel = pluginMessageChannel;
        }

        @NotNull
        public String getPluginMessageChannel() {
            return pluginMessageChannel;
        }
    }

}
