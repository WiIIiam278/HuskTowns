package me.william278.husktowns;

import java.util.ArrayList;

/**
 * This class calculates the limits of a town based on its' level. Based on config values
 */
public class TownLimitsCalculator {

    public static int getMaxClaims(int level) {
        return HuskTowns.getSettings().getMaxClaims().get(level-1);
    }

    public static int getMaxMembers(int level) {
        return HuskTowns.getSettings().getMaxMembers().get(level-1);
    }

    public static int getLevel(double coffers) {
        ArrayList<Double> requirements = HuskTowns.getSettings().getLevelRequirements();
        for (int i = 0; i < 20; i++) {
            if (coffers < requirements.get(i)) {
                return i;
            }
        }
        return 20;
    }
}
