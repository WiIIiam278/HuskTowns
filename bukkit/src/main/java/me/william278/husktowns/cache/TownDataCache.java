package me.william278.husktowns.cache;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.chunk.ClaimedChunk;
import me.william278.husktowns.flags.Flag;
import me.william278.husktowns.util.UpgradeUtil;

import java.util.HashMap;
import java.util.HashSet;

public class TownDataCache extends Cache {

    // HashMap of town greetings, farewells and bios
    private final HashMap<String, String> greetingMessages;
    private final HashMap<String, String> farewellMessages;
    private final HashMap<String, String> townBios;

    // HashMap of town flags for regular, farm & plot chunks
    private final HashMap<String, HashMap<ClaimedChunk.ChunkType, HashSet<Flag>>> townFlags;

    // HashSet of towns who have a publicly accessible spawn position
    private final HashSet<String> publicSpawnTowns;

    public TownDataCache() {
        super("Town Data");
        greetingMessages = new HashMap<>();
        farewellMessages = new HashMap<>();
        townBios = new HashMap<>();
        publicSpawnTowns = new HashSet<>();
        townFlags = new HashMap<>();
    }

    public void reload() {
        if (UpgradeUtil.getIsUpgrading()) {
            return;
        }
        if (getStatus() == CacheStatus.UPDATING) {
            return;
        }
        greetingMessages.clear();
        farewellMessages.clear();
        townBios.clear();
        publicSpawnTowns.clear();
        townFlags.clear();
        clearItemsLoaded();
        DataManager.updateTownDataCache();
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
        townFlags.put(newName, townFlags.remove(oldName));
    }

    public void disbandReload(String disbandingTown) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        greetingMessages.remove(disbandingTown);
        farewellMessages.remove(disbandingTown);
        townBios.remove(disbandingTown);
        townFlags.remove(disbandingTown);
        decrementItemsLoaded(6);
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

    public void setFlags(String town, HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> flags) {
        townFlags.put(town, flags);
        incrementItemsLoaded();
    }

    public void setFlag(String townName, ClaimedChunk.ChunkType flagToSetChunkType, String flagToSetIdentifier, boolean flagToSetValue) {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> flags = getAllFlags(townName);
        for (ClaimedChunk.ChunkType type : flags.keySet()) {
            if (type == flagToSetChunkType) {
                for (Flag flag : flags.get(type)) {
                    if (flag.getIdentifier().equalsIgnoreCase(flagToSetIdentifier)) {
                        flag.setFlag(flagToSetValue);
                        break;
                    }
                }
                break;
            }
        }
        townFlags.put(townName, flags);
    }


    public HashSet<String> getPublicSpawnTowns() {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
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

    public HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> getAllFlags(String townName) {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        return townFlags.get(townName);
    }

    public HashSet<Flag> getFlags(String town, ClaimedChunk.ChunkType chunkType) {
        if (town.equalsIgnoreCase(HuskTowns.getSettings().getAdminTownName())) {
            return HuskTowns.getSettings().getAdminClaimFlags().get(chunkType);
        }
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        return townFlags.get(town).get(chunkType);
    }

}
