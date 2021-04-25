package me.william278.husktowns.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.command.InviteCommand;
import me.william278.husktowns.data.pluginmessage.PluginMessage;
import me.william278.husktowns.object.TownInvite;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class PluginMessageListener implements org.bukkit.plugin.messaging.PluginMessageListener {

    private static void handlePluginMessage(PluginMessage pluginMessage, Player recipient) {
        switch (pluginMessage.getMessageType()) {
            case DEMOTED_NOTIFICATION:
                String[] demotionDetails = pluginMessage.getMessageData().split("\\$");
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "player_demoted",
                        demotionDetails[0], demotionDetails[1], demotionDetails[2]);
                return;
            case PROMOTED_NOTIFICATION:
                String[] promotionDetails = pluginMessage.getMessageData().split("\\$");
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "player_promoted",
                        promotionDetails[0], promotionDetails[1], promotionDetails[2]);
                return;
            case EVICTED_NOTIFICATION:
                String[] evictionDetails = pluginMessage.getMessageData().split("\\$");
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "player_evicted",
                        evictionDetails[0], evictionDetails[1]);
                return;
            case DEMOTED_NOTIFICATION_YOURSELF:
                String[] demotedDetails = pluginMessage.getMessageData().split("\\$");
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "have_been_demoted",
                        demotedDetails[0], demotedDetails[1]);
                return;
            case PROMOTED_NOTIFICATION_YOURSELF:
                String[] promotedDetails = pluginMessage.getMessageData().split("\\$");
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "have_been_promoted",
                        promotedDetails[0], promotedDetails[1]);
                return;
            case EVICTED_NOTIFICATION_YOURSELF:
                String[] evictedDetails = pluginMessage.getMessageData().split("\\$");
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "have_been_evicted",
                        evictedDetails[0], evictedDetails[1]);
                return;
            case INVITED_TO_JOIN:
                String[] inviteDetails = pluginMessage.getMessageData().split("\\$");
                InviteCommand.sendInvite(recipient, new TownInvite(inviteDetails[0],
                        UUID.fromString(inviteDetails[1]), Long.parseLong(inviteDetails[2])));
                return;
            case INVITED_TO_JOIN_REPLY:
                String[] replyDetails = pluginMessage.getMessageData().split("\\$");
                boolean accepted = Boolean.parseBoolean(replyDetails[0]);
                if (accepted) {
                    MessageManager.sendMessage(recipient, "invite_accepted", replyDetails[1], replyDetails[2]);
                } else {
                    MessageManager.sendMessage(recipient, "invite_rejected", replyDetails[1], replyDetails[2]);
                }
                return;
            case INVITED_NOTIFICATION:
                String[] invitedDetails = pluginMessage.getMessageData().split("\\$");
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "player_invited",
                        invitedDetails[0], invitedDetails[1]);
                return;
            case DISBAND_NOTIFICATION:
                String[] disbandedDetails = pluginMessage.getMessageData().split("\\$");
                MessageManager.sendMessage(recipient, "town_disbanded", disbandedDetails[0], disbandedDetails[1]);
                return;
            case PLAYER_HAS_JOINED_NOTIFICATION:
                String playerName = pluginMessage.getMessageData();
                MessageManager.sendMessage(recipient, "player_joined", playerName);
                return;
            case ADD_PLAYER_TO_CACHE:
                String[] playerDetails = pluginMessage.getMessageData().split("\\$");
                HuskTowns.getPlayerCache().setPlayerName(UUID.fromString(playerDetails[0]), playerDetails[1]);
                return;
            default:
                HuskTowns.getInstance().getLogger().log(Level.WARNING, "Received a HuskTowns plugin message with an unrecognised type. Is your version of HuskTowns up to date?");
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        // Return if the message is not a Bungee message
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput input = ByteStreams.newDataInput(message);

        // Plugin messages are formatted as such:
        // HuskTowns:<cluster_id>:<message_type>, followed by the message arguments and data.
        String messageType = input.readUTF();
        int clusterID;

        // Return if the message was not sent by HuskTowns
        if (!messageType.contains("HuskTowns:")) {
            return;
        }

        // Ensure the cluster ID matches
        try {
            clusterID = Integer.parseInt(messageType.split(":")[1]);
        } catch (Exception e) {
            // In case the message is malformed or the cluster ID is invalid
            HuskTowns.getInstance().getLogger().warning("Received a HuskTowns plugin message with an invalid server Cluster ID! \n" +
                    "Please ensure that the cluster ID is set to a valid integer on all servers.");
            return;
        }
        if (HuskTowns.getSettings().getClusterID() != clusterID) {
            return;
        }

        // Get the message data packets
        String messageData = "";
        short messageLength = input.readShort();
        byte[] messageBytes = new byte[messageLength];
        input.readFully(messageBytes);
        DataInputStream messageIn = new DataInputStream(new ByteArrayInputStream(messageBytes));

        // Get the message data string from the packets received
        try {
            messageData = messageIn.readUTF();
        } catch (IOException e) {
            Bukkit.getLogger().warning("An error occurred trying to read a plugin message (" + e.getCause() + ")");
            e.printStackTrace();
        }

        // Handle the plugin message appropriately
        handlePluginMessage(new PluginMessage(clusterID, player.getName(), messageType.split(":")[2], messageData), player);
    }

}