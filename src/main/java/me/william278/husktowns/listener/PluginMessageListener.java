package me.william278.husktowns.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.pluginmessage.PluginMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PluginMessageListener implements org.bukkit.plugin.messaging.PluginMessageListener {

    private static void handlePluginMessage(PluginMessage pluginMessage) {
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
            HuskTowns.getInstance().getLogger().warning("Received a HuskTowns plugin message with an invalid server cluster ID! \n" +
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
        handlePluginMessage(new PluginMessage(clusterID, player.getName(), messageType.split(":")[2], messageData));
    }

}
