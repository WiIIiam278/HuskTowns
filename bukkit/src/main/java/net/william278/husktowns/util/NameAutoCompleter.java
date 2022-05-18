package net.william278.husktowns.util;

import net.william278.husktowns.HuskTowns;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NameAutoCompleter {

    /*
     * Auto complete a player's name
     */
    public static String getAutoCompletedName(String inputName) {
        if (!HuskTowns.getSettings().autoCompletePlayerNames) {
            return inputName;
        }
        String found = inputName;

        // Try for an exact match first.
        Player localPlayer = Bukkit.getPlayerExact(inputName);
        if (localPlayer != null) {
            return localPlayer.getName();
        }

        // Iterate through players in the PlayerList to find a match
        String lowerName = inputName.toLowerCase();
        int delta = Integer.MAX_VALUE;
        for (String playerName : HuskTowns.getPlayerList().getPlayers()) {
            if (playerName.toLowerCase().startsWith(lowerName)) {
                int currentDelta = Math.abs(playerName.length() - lowerName.length());
                if (currentDelta < delta) {
                    found = playerName;
                    delta = currentDelta;
                }
                if (currentDelta == 0) break;
            }
        }
        return found;
    }

}
