package net.william278.husktowns.data.message;

import net.william278.husktowns.HuskTowns;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.StringJoiner;

public abstract class Message {

    private static final String MESSAGE_DATA_SEPARATOR = "$";

    private final int clusterId;
    private final MessageType messageType;
    private final String targetPlayerName;
    private final String messageData;

    public Message(String targetPlayerName, MessageType pluginMessageType, String... messageData) {
        this.clusterId = HuskTowns.getSettings().getClusterID();
        this.messageType = pluginMessageType;
        StringJoiner newMessageData = new StringJoiner(MESSAGE_DATA_SEPARATOR);
        for (String dataItem : messageData) {
            newMessageData.add(dataItem);
        }
        this.messageData = newMessageData.toString();
        this.targetPlayerName = targetPlayerName;
    }

    public Message(MessageType pluginMessageType, String... messageData) {
        this.clusterId = HuskTowns.getSettings().getClusterID();
        this.messageType = pluginMessageType;
        StringJoiner newMessageData = new StringJoiner(MESSAGE_DATA_SEPARATOR);
        for (String dataItem : messageData) {
            newMessageData.add(dataItem);
        }
        this.messageData = newMessageData.toString();
        this.targetPlayerName = null;
    }

    public Message(int clusterId, String targetPlayerName, String pluginMessageType, String... messageData) {
        this.clusterId = clusterId;
        this.messageType = MessageType.valueOf(pluginMessageType.toUpperCase(Locale.ENGLISH));
        StringJoiner newMessageData = new StringJoiner(MESSAGE_DATA_SEPARATOR);
        for (String dataItem : messageData) {
            newMessageData.add(dataItem);
        }
        this.messageData = newMessageData.toString();
        this.targetPlayerName = targetPlayerName;
    }

    // Get the string version of the plugin message type
    protected String getPluginMessageString(MessageType type) {
        return type.name().toLowerCase(Locale.ENGLISH);
    }

    public int getClusterId() {
        return clusterId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getTargetPlayerName() {
        return targetPlayerName;
    }

    public String getMessageData() {
        return messageData;
    }

    public String[] getMessageDataItems() {
        return messageData.split("\\" + MESSAGE_DATA_SEPARATOR);
    }

    public abstract void send(Player sender);

    public abstract void sendToAll(Player sender);

    public abstract void sendToServer(Player sender, String server);

    public enum MessageType {
        DEMOTED_NOTIFICATION_YOURSELF,
        DEMOTED_NOTIFICATION,
        PROMOTED_NOTIFICATION_YOURSELF,
        PROMOTED_NOTIFICATION,
        EVICTED_NOTIFICATION_YOURSELF,
        EVICTED_NOTIFICATION,
        DISBAND_NOTIFICATION,
        RENAME_NOTIFICATION,
        TRANSFER_NOTIFICATION,
        TRANSFER_YOU_NOTIFICATION,
        ADD_PLAYER_TO_CACHE,
        INVITED_NOTIFICATION,
        INVITED_TO_JOIN,
        INVITED_TO_JOIN_REPLY,
        PLAYER_HAS_JOINED_NOTIFICATION,
        DEPOSIT_NOTIFICATION,
        LEVEL_UP_NOTIFICATION,
        UPDATE_CACHED_GREETING_MESSAGE,
        UPDATE_CACHED_FAREWELL_MESSAGE,
        UPDATE_CACHED_BIO_MESSAGE,
        TOWN_RENAME,
        TOWN_DISBAND,
        TOWN_REMOVE_ALL_CLAIMS,
        REMOVE_ALL_CLAIMS_NOTIFICATION,
        SET_PLAYER_TOWN,
        SET_PLAYER_ROLE,
        CLEAR_PLAYER_TOWN,
        CLEAR_PLAYER_ROLE,
        TOWN_CHAT_MESSAGE,
        ADD_TOWN_BONUS,
        CLEAR_TOWN_BONUSES,
        SET_TOWN_SPAWN_PRIVACY,
        UPDATE_TOWN_FLAG,
        CREATE_TOWN_FLAGS,
        GET_PLAYER_LIST,
        RETURN_PLAYER_LIST
    }
}
