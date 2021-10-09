package me.william278.husktowns.util;

import me.william278.husktowns.data.DataManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class AutoClaimUtil {

    private static final HashMap<UUID, AutoClaimMode> autoClaimers = new HashMap<>();

    public static boolean isAutoClaiming(Player player) {
        return autoClaimers.containsKey(player.getUniqueId());
    }

    public static AutoClaimMode getAutoClaimType(Player player) {
        return autoClaimers.get(player.getUniqueId());
    }

    public static void addAutoClaimer(Player player, AutoClaimMode mode) {
        autoClaimers.put(player.getUniqueId(), mode);
    }

    public static void removeAutoClaimer(Player player) {
        autoClaimers.remove(player.getUniqueId());
    }

    public static void autoClaim(Player player, Location locationToClaim) {
        if (getAutoClaimType(player) == AutoClaimMode.REGULAR) {
            DataManager.claimChunk(player, locationToClaim, false);
        } else if (getAutoClaimType(player) == AutoClaimMode.ADMIN) {
            DataManager.createAdminClaim(player, locationToClaim, false);
        }
    }

    public enum AutoClaimMode {
        REGULAR,
        ADMIN
    }

}
