package me.william278.husktowns.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.command.InviteCommand;
import me.william278.husktowns.data.pluginmessage.PluginMessage;
import me.william278.husktowns.object.town.TownInvite;
import me.william278.husktowns.object.town.TownRole;
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
                String[] demotionDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "player_demoted",
                        demotionDetails[0], demotionDetails[1], demotionDetails[2]);
                return;
            case PROMOTED_NOTIFICATION:
                String[] promotionDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "player_promoted",
                        promotionDetails[0], promotionDetails[1], promotionDetails[2]);
                return;
            case EVICTED_NOTIFICATION:
                String[] evictionDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "player_evicted",
                        evictionDetails[0], evictionDetails[1]);
                return;
            case DEMOTED_NOTIFICATION_YOURSELF:
                String[] demotedDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "have_been_demoted",
                        demotedDetails[0], demotedDetails[1]);
                return;
            case PROMOTED_NOTIFICATION_YOURSELF:
                String[] promotedDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "have_been_promoted",
                        promotedDetails[0], promotedDetails[1]);
                return;
            case EVICTED_NOTIFICATION_YOURSELF:
                String[] evictedDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "have_been_evicted",
                        evictedDetails[0], evictedDetails[1]);
                return;
            case TRANSFER_YOU_NOTIFICATION:
                String[] transferredDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(recipient, "town_transferred_to_you", transferredDetails[0], transferredDetails[1]);
                return;
            case DEPOSIT_NOTIFICATION:
                String[] depositDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(recipient, "town_deposit_notification", depositDetails[0], depositDetails[1]);
                return;
            case LEVEL_UP_NOTIFICATION:
                String[] levelUpDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(recipient, "town_level_up_notification", levelUpDetails[0], levelUpDetails[1], levelUpDetails[2]);
                return;
            case INVITED_TO_JOIN:
                String[] inviteDetails = pluginMessage.getMessageDataItems();
                InviteCommand.sendInvite(recipient, new TownInvite(inviteDetails[0],
                        inviteDetails[1], Long.parseLong(inviteDetails[2])));
                return;
            case INVITED_TO_JOIN_REPLY:
                String[] replyDetails = pluginMessage.getMessageDataItems();
                boolean accepted = Boolean.parseBoolean(replyDetails[0]);
                if (accepted) {
                    MessageManager.sendMessage(recipient, "invite_accepted", replyDetails[1], replyDetails[2]);
                } else {
                    MessageManager.sendMessage(recipient, "invite_rejected", replyDetails[1], replyDetails[2]);
                }
                return;
            case INVITED_NOTIFICATION:
                String[] invitedDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "player_invited",
                        invitedDetails[0], invitedDetails[1]);
                return;
            case DISBAND_NOTIFICATION:
                String[] disbandedDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(recipient, "town_disbanded", disbandedDetails[0], disbandedDetails[1]);
                return;
            case RENAME_NOTIFICATION:
                String[] renameDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(recipient, "town_renamed", renameDetails[0], renameDetails[1]);
                return;
            case TRANSFER_NOTIFICATION:
                String[] transferDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(recipient, "town_transferred", transferDetails[0], transferDetails[1], transferDetails[2]);
                return;
            case PLAYER_HAS_JOINED_NOTIFICATION:
                String playerName = pluginMessage.getMessageData();
                MessageManager.sendMessage(recipient, "player_joined", playerName);
                return;
            case ADD_PLAYER_TO_CACHE:
                String[] playerDetails = pluginMessage.getMessageDataItems();
                HuskTowns.getPlayerCache().setPlayerName(UUID.fromString(playerDetails[0]), playerDetails[1]);
                return;
            case UPDATE_CACHED_GREETING_MESSAGE:
                String[] newGreetingDetails = pluginMessage.getMessageDataItems();
                HuskTowns.getTownMessageCache().setGreetingMessage(newGreetingDetails[0], newGreetingDetails[1]);
                return;
            case UPDATE_CACHED_FAREWELL_MESSAGE:
                String[] newFarewellDetails = pluginMessage.getMessageDataItems();
                HuskTowns.getTownMessageCache().setFarewellMessage(newFarewellDetails[0], newFarewellDetails[1]);
                return;
            case TOWN_DISBAND:
                String disbandingTown = pluginMessage.getMessageData();
                HuskTowns.getClaimCache().disbandReload(disbandingTown);
                HuskTowns.getPlayerCache().disbandReload(disbandingTown);
                HuskTowns.getTownMessageCache().disbandReload(disbandingTown);
                HuskTowns.getTownBonusesCache().disbandReload(disbandingTown);
                return;
            case TOWN_RENAME:
                String[] renamingDetails = pluginMessage.getMessageDataItems();
                String oldName = renamingDetails[0];
                String newName = renamingDetails[1];
                HuskTowns.getClaimCache().renameReload(oldName, newName);
                HuskTowns.getPlayerCache().renameReload(oldName, newName);
                HuskTowns.getTownMessageCache().renameReload(oldName, newName);
                HuskTowns.getTownBonusesCache().renameReload(oldName, newName);
                return;
            case UPDATE_PLAYER_TOWN:
                String[] newPlayerTownDetails = pluginMessage.getMessageDataItems();
                UUID playerToUpdate = UUID.fromString(newPlayerTownDetails[0]);
                String playerTown = newPlayerTownDetails[1];
                HuskTowns.getPlayerCache().setPlayerTown(playerToUpdate, playerTown);
                return;
            case UPDATE_PLAYER_ROLE:
                String[] newPlayerRoleDetails = pluginMessage.getMessageDataItems();
                UUID playerRoleToUpdate = UUID.fromString(newPlayerRoleDetails[0]);
                TownRole role = TownRole.valueOf(newPlayerRoleDetails[1]);
                HuskTowns.getPlayerCache().setPlayerRole(playerRoleToUpdate, role);
                return;
            case TOWN_CHAT_MESSAGE:
                String[] messageData = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(recipient, "town_chat", messageData[0], messageData[1], messageData[2].replaceAll("\\💲", "$"));
                return;
            case UPDATE_TOWN_BONUSES:
                HuskTowns.getTownBonusesCache().reload();
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