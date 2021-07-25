package me.william278.husktowns.object.cache;

import me.william278.husktowns.data.DataManager;

import java.util.HashMap;
import java.util.HashSet;

public class TownDataCache extends Cache {

    private final HashMap<String,String> greetingMessages;
    private final HashMap<String,String> farewellMessages;
    private final HashMap<String,String> townBios;
    private final HashSet<String> publicSpawnTowns;

    public TownDataCache() {
        super("Town Data");
        greetingMessages = new HashMap<>();
        farewellMessages = new HashMap<>();
        townBios = new HashMap<>();
        publicSpawnTowns = new HashSet<>();
        reload();
    }

    public void reload() {
        greetingMessages.clear();
        farewellMessages.clear();
        townBios.clear();
        publicSpawnTowns.clear();
        DataManager.updateTownInfoCache();
        clearItemsLoaded();
    }

    public void renameReload(String oldName, String newName) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        greetingMessages.put(newName, greetingMessages.remove(oldName));
        farewellMessages.put(newName, farewellMessages.remove(oldName));
        townBios.put(newName, townBios.remove(oldName));
        if (publicSpawnTowns.contains(oldName)) {
            publicSpawnTowns.remove(oldName);
            publicSpawnTowns.add(newName);
        }
    }

    public void disbandReload(String disbandingTown) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        greetingMessages.remove(disbandingTown);
        decrementItemsLoaded();
        farewellMessages.remove(disbandingTown);
        decrementItemsLoaded();
        townBios.remove(disbandingTown);
        decrementItemsLoaded();
    }

    public void setTownBio(String town, String message) {
        townBios.put(town, message);
        incrementItemsLoaded();
    }

    public void setGreetingMessage(String town, String message) {
        greetingMessages.put(town, message);
        incrementItemsLoaded();
    }

    public void setFarewellMessage(String town, String message) {
        farewellMessages.put(town, message);
        incrementItemsLoaded();
    }

    public void addTownWithPublicSpawn(String town) {
        publicSpawnTowns.add(town);
        incrementItemsLoaded();
    }

    public void removeTownWithPublicSpawn(String town) {
        publicSpawnTowns.remove(town);
        decrementItemsLoaded();
    }

    public HashSet<String> getPublicSpawnTowns() {
        return publicSpawnTowns;
    }

    public String getTownBio(String town) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        return townBios.get(town);
    }

    public String getGreetingMessage(String town) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        return greetingMessages.get(town);
    }

    public String getFarewellMessage(String town) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        return farewellMessages.get(town);
    }

}
