package net.william278.husktowns.data.message.redis;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.data.message.CrossServerMessageHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class RedisReceiver {

    private static final HuskTowns plugin = HuskTowns.getInstance();
    public static final String REDIS_CHANNEL = "HuskTowns";

    public static void listen() {
        Jedis jedis = new Jedis(HuskTowns.getSettings().getRedisHost(), HuskTowns.getSettings().getRedisPort());
        final String jedisPassword = HuskTowns.getSettings().getRedisPassword();
        if (!jedisPassword.equals("")) {
            jedis.auth(jedisPassword);
        }
        jedis.connect();
        if (jedis.isConnected()) {
            plugin.getLogger().info("Enabled Redis listener successfully!");
            new Thread(() -> jedis.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    if (!channel.equals(REDIS_CHANNEL)) {
                        return;
                    }

                    /* HuskTowns Redis messages have a header formatted as such:
                     * <cluster_id>:<message_type>:<target_player>£ followed by the message arguments and data. */
                    final String[] splitMessage = message.split(RedisMessage.REDIS_MESSAGE_HEADER_SEPARATOR);
                    final String messageHeader = splitMessage[0];
                    int clusterID;

                    // Ensure the cluster ID matches
                    try {
                        clusterID = Integer.parseInt(messageHeader.split(":")[0]);
                    } catch (Exception e) {
                        // In case the message is malformed or the cluster ID is invalid
                        HuskTowns.getInstance().getLogger().warning("Received a Redis message on the HuskTowns channel with an invalid server Cluster ID! \n" +
                                "Please ensure that the cluster ID is set to a valid integer on all servers.");
                        return;
                    }
                    if (HuskTowns.getSettings().getClusterID() != clusterID) {
                        return;
                    }

                    // Get the type of redis message
                    final String messageType = messageHeader.split(":")[1];

                    // Get the player targeted by the message, if applicable
                    final String target = messageHeader.split(":")[2];
                    Player receiver;
                    if (target.equalsIgnoreCase("-all-")) { // If the redis message was targeting all servers
                        receiver = null;
                    } else if (target.contains("server-")) { // If the redis message was targeting this server
                        if (target.split("-")[1].equalsIgnoreCase(HuskTowns.getSettings().getServerID())) {
                            receiver = null;
                        } else {
                            return; // The message was targeting another server; ignore
                        }
                    } else {
                        receiver = Bukkit.getPlayerExact(target);
                    }

                    // Determine the receiver name
                    String receiverName;
                    if (receiver == null) {
                        receiverName = null;
                    } else {
                        receiverName = receiver.getName();
                    }

                    final String messageData = splitMessage[1];
                    CrossServerMessageHandler.handleMessage(new RedisMessage(clusterID, receiverName, messageType, messageData), receiver);
                }
            }, REDIS_CHANNEL), "Redis Subscriber").start();
        } else {
            plugin.getLogger().severe("Failed to initialize the redis listener!");
        }
    }
}
