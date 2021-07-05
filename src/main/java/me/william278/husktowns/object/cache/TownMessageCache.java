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
        greetingMessages.put(newName, greetingMessages.remove(oldName));
        farewellMessages.put(newName, greetingMessages.remove(oldName));
    }

    public void disbandReload(String disbandingTown) {
        greetingMessages.remove(disbandingTown);
        farewellMessages.remove(disbandingTown);
    }

    public void setGreetingMessage(String town, String message) {
        greetingMessages.put(town, message);
    }

    public void setFarewellMessage(String town, String message) {
        farewellMessages.put(town, message);
    }

    public String getGreetingMessage(String town) {
        return greetingMessages.get(town);
    }

    public String getFarewellMessage(String town) {
        return farewellMessages.get(town);
    }

}
