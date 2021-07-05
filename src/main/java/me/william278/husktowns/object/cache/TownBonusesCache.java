package me.william278.husktowns.object.cache;

import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.object.town.TownBonus;

import java.util.HashMap;
import java.util.HashSet;

public class TownBonusesCache extends Cache {

    private final HashMap<String,HashSet<TownBonus>> townBonuses;

    public TownBonusesCache() {
        super("Town Bonuses");
        townBonuses = new HashMap<>();
        reload();
    }

    public void reload() {
        townBonuses.clear();
        DataManager.updateCachedBonuses();
    }

    public void renameReload(String oldName, String newName) {
        townBonuses.put(newName, townBonuses.remove(oldName));
    }

    public void disbandReload(String disbandingTown) {
        townBonuses.remove(disbandingTown);
    }

    public boolean contains(String townName) {
        return townBonuses.containsKey(townName);
    }

    public void add(String townName, TownBonus townBonus) {
        if (!townBonuses.containsKey(townName)) {
            townBonuses.put(townName, new HashSet<>());
        }
        HashSet<TownBonus> currentBonuses = getTownBonuses(townName);
        currentBonuses.add(townBonus);
        townBonuses.put(townName, currentBonuses);
    }

    public HashSet<TownBonus> getTownBonuses(String townName) {
        if (townBonuses.containsKey(townName)) {
            return townBonuses.get(townName);
        } else {
            return new HashSet<>();
        }
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
