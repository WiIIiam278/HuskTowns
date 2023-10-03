/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.husktowns.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a message sent by a {@link Broker} cross-server. See {@link #builder()} for
 * a builder to create a message.
 */
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

    /**
     * Builder for {@link Message}s
     */
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

    /**
     * Different types of cross-server messages
     */
    public enum Type {
        /**
         * Indicates the target server should pull and cache new town data from the database for a town by ID.
         */
        TOWN_UPDATE,
        /**
         * Indicates the target server should remove all claims for a town by ID and remove it from the cache.
         */
        TOWN_DELETE,
        /**
         * Indicates the target server should remove all claims for a town by ID from its claim world cache
         */
        TOWN_DELETE_ALL_CLAIMS,
        /**
         * A message sent to handle a cross-server invite
         */
        TOWN_INVITE_REQUEST,
        /**
         * A message send to reply to a cross-server invite
         */
        TOWN_INVITE_REPLY,
        /**
         * A notification message that a town has leveled up
         */
        TOWN_LEVEL_UP,
        /**
         * A notification message that a town has transferred ownership
         */
        TOWN_TRANSFERRED,
        /**
         * A notification message that a town has evicted a member
         */
        TOWN_EVICTED,
        /**
         * A notification message that a town has promoted a member
         */
        TOWN_PROMOTED,
        /**
         * A notification message that a town has demoted a member
         */
        TOWN_DEMOTED,
        /**
         * A notification message that a town has renamed
         */
        TOWN_RENAMED,
        /**
         * A message for dispatching a cross-server war declaration
         */
        TOWN_WAR_DECLARATION_SENT,
        /**
         * A message for dispatching a cross-server war declaration acceptance
         */
        TOWN_WAR_DECLARATION_ACCEPTED,
        /**
         * A message for dispatching a cross-server town chat message
         */
        TOWN_CHAT_MESSAGE,
        /**
         * Request other servers for a list of online users
         */
        REQUEST_USER_LIST,
        /**
         * A message requesting that a server end a war for a town (by integer ID payload) if there is an active
         * war involving them.
         */
        TOWN_WAR_END,
        /**
         * A message containing a list of users on a server
         */
        USER_LIST
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
