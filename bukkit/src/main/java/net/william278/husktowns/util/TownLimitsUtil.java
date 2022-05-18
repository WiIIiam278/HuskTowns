package net.william278.husktowns.util;

import net.william278.husktowns.HuskTowns;

import java.util.ArrayList;

/**
 * This class calculates the limits of a town based on its' level. Based on config values
 */
public class TownLimitsUtil {

    public static int getMaxClaims(int level, String townName) {
        if (HuskTowns.getTownBonusesCache().contains(townName)) {
            return HuskTowns.getSettings().maxClaims.get(level - 1) + HuskTowns.getTownBonusesCache().getBonusClaims(townName);
        } else {
            return HuskTowns.getSettings().maxClaims.get(level - 1);
        }
    }

    public static int getMaxMembers(int level, String townName) {
        if (HuskTowns.getTownBonusesCache().contains(townName)) {
            return HuskTowns.getSettings().maxMembers.get(level - 1) + HuskTowns.getTownBonusesCache().getBonusMembers(townName);
        } else {
            return HuskTowns.getSettings().maxMembers.get(level - 1);
        }
    }

    public static int getLevel(double coffers) {
        ArrayList<Double> requirements = HuskTowns.getSettings().levelRequirements;
        int maxLevel = requirements.size();
        for (int i = 0; i < maxLevel; i++) {
            if (coffers < requirements.get(i)) {
                return i;
            }
        }
        return maxLevel;
    }

    // Returns the amount needed to reach the next level; -1 if the town is max level
    public static double getNextLevelRequired(double coffers) {
        ArrayList<Double> requirements = HuskTowns.getSettings().levelRequirements;
        for (Double requirement : requirements) {
            double amountToNextLevel = requirement - coffers;
            if (amountToNextLevel > 0) {
                return amountToNextLevel;
            }
        }
        return -1D;
    }
}
