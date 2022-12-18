package net.william278.husktowns.network;

import com.google.gson.annotations.Expose;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

public class Message {
    @NotNull
    @Expose
    private Type type;
    @NotNull
    @Expose
    private String target;
    @NotNull
    @Expose
    private Payload payload;
    @NotNull
    @Expose
    private String sender;

    private Message(@NotNull Type type, @NotNull String target, @NotNull Payload payload) {
        this.type = type;
        this.target = target;
        this.payload = payload;
    }

    @SuppressWarnings("unused")
    private Message() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public void send(@NotNull Broker broker, @NotNull OnlineUser sender) {
        this.sender = sender.getUsername();
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
    public Payload getPayload() {
        return payload;
    }

    @NotNull
    public String getSender() {
        return sender;
    }

    public static class Builder {
        private Type type;
        private Payload payload = Payload.empty();
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
        public Builder target(@NotNull String target) {
            this.target = target;
            return this;
        }

        @NotNull
        public Message build() {
            return new Message(type, target, payload);
        }

    }

    public enum Type {
        /**
         * Payload contains a UUID of a town.
         * Indicates the target server should pull and cache new town data from the database for that town.
         */
        TOWN_UPDATE,
        /**
         * Payload contains a UUID of a town.
         * Indicates the target server should remove all claims for that town and remove the town from memory.
         */
        TOWN_DELETE;
    }

}
