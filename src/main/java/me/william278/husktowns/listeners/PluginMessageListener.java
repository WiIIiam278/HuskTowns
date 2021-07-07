package me.william278.husktowns.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.commands.InviteCommand;
import me.william278.husktowns.commands.TownChatCommand;
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
                final String[] demotionDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "player_demoted",
                        demotionDetails[0], demotionDetails[1], demotionDetails[2]);
                break;
            case PROMOTED_NOTIFICATION:
                final String[] promotionDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "player_promoted",
                        promotionDetails[0], promotionDetails[1], promotionDetails[2]);
                break;
            case EVICTED_NOTIFICATION:
                final String[] evictionDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "player_evicted",
                        evictionDetails[0], evictionDetails[1]);
                break;
            case DEMOTED_NOTIFICATION_YOURSELF:
                final String[] demotedDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "have_been_demoted",
                        demotedDetails[0], demotedDetails[1]);
                break;
            case PROMOTED_NOTIFICATION_YOURSELF:
                final String[] promotedDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "have_been_promoted",
                        promotedDetails[0], promotedDetails[1]);
                break;
            case EVICTED_NOTIFICATION_YOURSELF:
                final String[] evictedDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "have_been_evicted",
                        evictedDetails[0], evictedDetails[1]);
                break;
            case TRANSFER_YOU_NOTIFICATION:
                final String[] transferredDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(recipient, "town_transferred_to_you", transferredDetails[0], transferredDetails[1]);
                break;
            case DEPOSIT_NOTIFICATION:
                final String[] depositDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(recipient, "town_deposit_notification", depositDetails[0], depositDetails[1]);
                break;
            case LEVEL_UP_NOTIFICATION:
                final String[] levelUpDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(recipient, "town_level_up_notification", levelUpDetails[0], levelUpDetails[1], levelUpDetails[2]);
                break;
            case INVITED_TO_JOIN:
                final String[] inviteDetails = pluginMessage.getMessageDataItems();
                InviteCommand.sendInvite(recipient, new TownInvite(inviteDetails[0],
                        inviteDetails[1], Long.parseLong(inviteDetails[2])));
                break;
            case INVITED_TO_JOIN_REPLY:
                String[] replyDetails = pluginMessage.getMessageDataItems();
                boolean accepted = Boolean.parseBoolean(replyDetails[0]);
                if (accepted) {
                    MessageManager.sendMessage(recipient, "invite_accepted", replyDetails[1], replyDetails[2]);
                } else {
                    MessageManager.sendMessage(recipient, "invite_rejected", replyDetails[1], replyDetails[2]);
                }
                break;
            case INVITED_NOTIFICATION:
                final String[] invitedDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(Bukkit.getPlayer(pluginMessage.getTargetPlayerName()), "player_invited",
                        invitedDetails[0], invitedDetails[1]);
                break;
            case DISBAND_NOTIFICATION:
                final String[] disbandedDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(recipient, "town_disbanded", disbandedDetails[0], disbandedDetails[1]);
                break;
            case RENAME_NOTIFICATION:
                final String[] renameDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(recipient, "town_renamed", renameDetails[0], renameDetails[1]);
                break;
            case TRANSFER_NOTIFICATION:
                final String[] transferDetails = pluginMessage.getMessageDataItems();
                MessageManager.sendMessage(recipient, "town_transferred", transferDetails[0], transferDetails[1], transferDetails[2]);
                break;
            case PLAYER_HAS_JOINED_NOTIFICATION:
                final String playerName = pluginMessage.getMessageData();
                MessageManager.sendMessage(recipient, "player_joined", playerName);
                break;
            case ADD_PLAYER_TO_CACHE:
                if (!HuskTowns.getPlayerCache().hasLoaded()) {
                    break;
                }
                final String[] playerDetails = pluginMessage.getMessageDataItems();
                HuskTowns.getPlayerCache().setPlayerName(UUID.fromString(playerDetails[0]), playerDetails[1]);
                break;
            case UPDATE_CACHED_GREETING_MESSAGE:
                if (!HuskTowns.getTownMessageCache().hasLoaded()) {
                    break;
                }
                final String[] newGreetingDetails = pluginMessage.getMessageDataItems();
                HuskTowns.getTownMessageCache().setGreetingMessage(newGreetingDetails[0], newGreetingDetails[1]);
                break;
            case UPDATE_CACHED_FAREWELL_MESSAGE:
                if (!HuskTowns.getTownMessageCache().hasLoaded()) {
                    break;
                }
                final String[] newFarewellDetails = pluginMessage.getMessageDataItems();
                HuskTowns.getTownMessageCache().setFarewellMessage(newFarewellDetails[0], newFarewellDetails[1]);
                break;
            case TOWN_DISBAND:
                final String disbandingTown = pluginMessage.getMessageData();
                if (HuskTowns.getClaimCache().hasLoaded()) {
                    HuskTowns.getClaimCache().disbandReload(disbandingTown);
                }
                if (HuskTowns.getPlayerCache().hasLoaded()) {
                    HuskTowns.getPlayerCache().disbandReload(disbandingTown);
                }
                if (HuskTowns.getTownBonusesCache().hasLoaded()) {
                    HuskTowns.getTownBonusesCache().disbandReload(disbandingTown);
                }
                if (HuskTowns.getTownMessageCache().hasLoaded()) {
                    HuskTowns.getTownMessageCache().disbandReload(disbandingTown);
                }
                break;
            case TOWN_RENAME:
                final String[] renamingDetails = pluginMessage.getMessageDataItems();
                final String oldName = renamingDetails[0];
                final String newName = renamingDetails[1];
                if (!HuskTowns.getClaimCache().hasLoaded()) {
                    HuskTowns.getClaimCache().renameReload(oldName, newName);
                }
                if (!HuskTowns.getPlayerCache().hasLoaded()) {
                    HuskTowns.getPlayerCache().renameReload(oldName, newName);
                }
                if (!HuskTowns.getTownBonusesCache().hasLoaded()) {
                    HuskTowns.getTownBonusesCache().renameReload(oldName, newName);
                }
                if (!HuskTowns.getTownMessageCache().hasLoaded()) {
                    HuskTowns.getTownMessageCache().renameReload(oldName, newName);
                }
                break;
            case SET_PLAYER_TOWN:
                if (!HuskTowns.getPlayerCache().hasLoaded()) {
                    break;
                }
                final String[] newPlayerTownDetails = pluginMessage.getMessageDataItems();
                final UUID playerToUpdate = UUID.fromString(newPlayerTownDetails[0]);
                final String playerTown = newPlayerTownDetails[1];
                HuskTowns.getPlayerCache().setPlayerTown(playerToUpdate, playerTown);
                break;
            case CLEAR_PLAYER_TOWN:
                if (!HuskTowns.getPlayerCache().hasLoaded()) {
                    break;
                }
                final UUID townMemberToClear = UUID.fromString(pluginMessage.getMessageData());
                HuskTowns.getPlayerCache().clearPlayerTown(townMemberToClear);
                break;
            case SET_PLAYER_ROLE:
                if (!HuskTowns.getPlayerCache().hasLoaded()) {
                    break;
                }
                final String[] newPlayerRoleDetails = pluginMessage.getMessageDataItems();
                final UUID playerRoleToUpdate = UUID.fromString(newPlayerRoleDetails[0]);
                final TownRole role = TownRole.valueOf(newPlayerRoleDetails[1]);
                HuskTowns.getPlayerCache().setPlayerRole(playerRoleToUpdate, role);
                break;
            case CLEAR_PLAYER_ROLE:
                if (!HuskTowns.getPlayerCache().hasLoaded()) {
                    break;
                }
                final UUID roleHolderToClear = UUID.fromString(pluginMessage.getMessageData());
                HuskTowns.getPlayerCache().clearPlayerRole(roleHolderToClear);
                break;
            case TOWN_CHAT_MESSAGE:
                final String[] messageData = pluginMessage.getMessageDataItems();
                final String message = messageData[2].replaceAll("💲", "$");
                Bukkit.getScheduler().runTaskAsynchronously(HuskTowns.getInstance(), () -> TownChatCommand.dispatchTownMessage(messageData[0], messageData[1], message));
                break;
            case UPDATE_TOWN_BONUSES:
                HuskTowns.getTownBonusesCache().reload();
                break;
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