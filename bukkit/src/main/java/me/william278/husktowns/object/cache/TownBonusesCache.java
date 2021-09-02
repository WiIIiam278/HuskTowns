package me.william278.husktowns.object.cache;

import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.object.town.TownBonus;
import me.william278.husktowns.util.UpgradeUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class TownBonusesCache extends Cache {

    private final HashMap<String, ArrayList<TownBonus>> townBonuses;

    public TownBonusesCache() {
        super("Town Bonuses");
        townBonuses = new HashMap<>();
    }

    public void reload() {
        if (UpgradeUtil.getIsUpgrading()) {
            return;
        }
        townBonuses.clear();
        clearItemsLoaded();
        DataManager.updateTownBonusCache();
    }

    public void renameTown(String oldName, String newName) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        if (townBonuses.containsKey(oldName)) {
            townBonuses.put(newName, townBonuses.remove(oldName));
        }
    }

    public void clearTownBonuses(String townToClear) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        ArrayList<TownBonus> bonuses = townBonuses.get(townToClear);
        if (bonuses != null) {
            for (TownBonus ignored : bonuses) {
                decrementItemsLoaded();
            }
        }
        townBonuses.remove(townToClear);
    }

    public boolean contains(String townName) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        return townBonuses.containsKey(townName);
    }

    public void add(String townName, TownBonus townBonus) {
        if (!townBonuses.containsKey(townName)) {
            townBonuses.put(townName, new ArrayList<>());
        }
        ArrayList<TownBonus> currentBonuses = getTownBonuses(townName);
        currentBonuses.add(townBonus);
        townBonuses.put(townName, currentBonuses);
        incrementItemsLoaded();
    }

    public ArrayList<TownBonus> getTownBonuses(String townName) {
        if (townBonuses.containsKey(townName)) {
            return townBonuses.get(townName);
        } else {
            return new ArrayList<>();
        }
    }

    public int getBonusMembers(String townName) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        int bonusMembers = 0;
        final ArrayList<TownBonus> townBonuses = new ArrayList<>(getTownBonuses(townName));
        for (TownBonus bonus : townBonuses) {
            bonusMembers = bonusMembers + bonus.getBonusMembers();
        }
        return bonusMembers;
    }

    public int getBonusClaims(String townName) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        int bonusClaims = 0;
        for (TownBonus bonus : getTownBonuses(townName)) {
            bonusClaims = bonusClaims + bonus.getBonusClaims();
        }
        return bonusClaims;
    }

}
