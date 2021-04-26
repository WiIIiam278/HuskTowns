package me.william278.husktowns.object.cache;

import me.william278.husktowns.data.DataManager;

import java.util.HashMap;

public class TownMessageCache {

    private final HashMap<String,String> welcomeMessages;
    private final HashMap<String,String> farewellMessages;

    public TownMessageCache() {
        welcomeMessages = new HashMap<>();
        farewellMessages = new HashMap<>();
        reload();
    }

    public void reload() {
        welcomeMessages.clear();
        farewellMessages.clear();
        DataManager.updateTownMessageCache();
    }

    public void setWelcomeMessage(String town, String message) {
        welcomeMessages.put(town, message);
    }

    public void setFarewellMessage(String town, String message) {
        farewellMessages.put(town, message);
    }

    public String getWelcomeMessage(String town) {
        return welcomeMessages.get(town);
    }

    public String getFarewellMessage(String town) {
        return farewellMessages.get(town);
    }

}
