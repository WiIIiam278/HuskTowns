package net.william278.husktowns.util;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.data.message.CrossServerMessageHandler;
import net.william278.husktowns.data.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;

public class PlayerList {

    private static final HuskTowns plugin = HuskTowns.getInstance();
    private static final long playerListUpdateTime = 60 * 20L;

    private static HashSet<String> players;

    public PlayerList() {
        players = new HashSet<>();
    }

    public void initialize() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                updateList(p);
                break;
            }
        }, 0, playerListUpdateTime));
    }

    public void updateList(Player updateRequester) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            players.clear();
            if (HuskTowns.getSettings().doBungee) {
                Bukkit.getScheduler().runTask(plugin, () -> CrossServerMessageHandler.getMessage(Message.MessageType.GET_PLAYER_LIST,
                        HuskTowns.getSettings().serverId).sendToAll(updateRequester));
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
        });
    }

    public HashSet<String> getPlayers() {
        return players;
    }

    public void addPlayer(String player) {
        players.add(player);
    }

    public void addPlayers(String[] playerList) {
        players.addAll(Arrays.asList(playerList));
    }

}
