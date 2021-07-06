package me.william278.husktowns.object.cache;

import me.william278.husktowns.data.DataManager;

import java.util.HashMap;

public class TownMessageCache extends Cache {

    private final HashMap<String,String> greetingMessages;
    private final HashMap<String,String> farewellMessages;

    public TownMessageCache() {
        super("Town Messages");
        greetingMessages = new HashMap<>();
        farewellMessages = new HashMap<>();
        reload();
    }

    public void reload() {
        greetingMessages.clear();
        farewellMessages.clear();
        DataManager.updateTownMessageCache();
    }

    public void renameReload(String oldName, String newName) {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        greetingMessages.put(newName, greetingMessages.remove(oldName));
        farewellMessages.put(newName, greetingMessages.remove(oldName));
    }

    public void disbandReload(String disbandingTown) {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        greetingMessages.remove(disbandingTown);
        farewellMessages.remove(disbandingTown);
    }

    public void setGreetingMessage(String town, String message) {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        greetingMessages.put(town, message);
    }

    public void setFarewellMessage(String town, String message) {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        farewellMessages.put(town, message);
    }

    public String getGreetingMessage(String town) {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        return greetingMessages.get(town);
    }

    public String getFarewellMessage(String town) {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        return farewellMessages.get(town);
    }

}
