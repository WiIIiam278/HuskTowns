package me.william278.husktowns.util;

import me.william278.husktowns.data.DataManager;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;

public class AutoClaimUtil {

    private static final HashSet<UUID> autoClaimers = new HashSet<>();

    public static boolean isAutoClaiming(Player player) {
        return autoClaimers.contains(player.getUniqueId());
    }

    public static void addAutoClaimer(Player player) {
        autoClaimers.add(player.getUniqueId());
    }

    public static void removeAutoClaimer(Player player) {
        autoClaimers.remove(player.getUniqueId());
    }

    public static void autoClaim(Player player) {
        if (!isAutoClaiming(player)) {
            return;
        }
        DataManager.claimChunk(player);
    }

}
