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
        clearItemsLoaded();
    }

    public void renameReload(String oldName, String newName) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        greetingMessages.put(newName, greetingMessages.remove(oldName));
        farewellMessages.put(newName, greetingMessages.remove(oldName));
    }

    public void disbandReload(String disbandingTown) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        greetingMessages.remove(disbandingTown);
        decrementItemsLoaded();
        farewellMessages.remove(disbandingTown);
        decrementItemsLoaded();
    }

    public void setGreetingMessage(String town, String message) {
        greetingMessages.put(town, message);
        incrementItemsLoaded();
    }

    public void setFarewellMessage(String town, String message) {
        farewellMessages.put(town, message);
        incrementItemsLoaded();
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
