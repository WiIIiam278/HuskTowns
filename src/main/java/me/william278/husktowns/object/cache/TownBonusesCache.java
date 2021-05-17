package me.william278.husktowns.object.cache;

import me.william278.husktowns.object.town.TownBonus;

import java.util.HashMap;
import java.util.HashSet;

public class TownBonusesCache {

    private final HashMap<String,TownBonus> townBonuses;


    public TownBonusesCache() {
        townBonuses = new HashMap<>();
    }

    public void reload() {
        townBonuses.clear();
        //todo get Town Bonuses from Data Manager
    }

    public void addTownBonus(String townName, TownBonus townBonus) {
        townBonuses.put(townName, townBonus);
    }

    public HashSet<TownBonus> getTownBonuses(String townName) {
        HashSet<TownBonus> bonuses = new HashSet<>();
        for (String town : townBonuses.keySet()) {
            if (townName.equals(town)) {
                bonuses.add(townBonuses.get(town));
            }
        }
        return bonuses;
    }

    public int getBonusMembers(String townName) {
        int bonusMembers = 0;
        for (TownBonus bonus : getTownBonuses(townName)) {
            bonusMembers = bonusMembers + bonus.getBonusMembers();
        }
        return bonusMembers;
    }

    public int getBonusClaims(String townName) {
        int bonusClaims = 0;
        for (TownBonus bonus : getTownBonuses(townName)) {
            bonusClaims = bonusClaims + bonus.getBonusClaims();
        }
        return bonusClaims;
    }

}
