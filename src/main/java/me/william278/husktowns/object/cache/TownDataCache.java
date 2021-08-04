package me.william278.husktowns.object.cache;

import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.object.chunk.ClaimedChunk;
import me.william278.husktowns.object.flag.Flag;

import java.util.HashMap;
import java.util.HashSet;

public class TownDataCache extends Cache {

    // HashMap of town greetings, farewells and bios
    private final HashMap<String, String> greetingMessages;
    private final HashMap<String, String> farewellMessages;
    private final HashMap<String, String> townBios;

    // HashMap of town flags for regular, farm & plot chunks
    private final HashMap<String, HashSet<Flag>> regularChunkFlags;
    private final HashMap<String, HashSet<Flag>> farmChunkFlags;
    private final HashMap<String, HashSet<Flag>> plotChunkFlags;

    // HashSet of towns who have a publicly accessible spawn position
    private final HashSet<String> publicSpawnTowns;


    public TownDataCache() {
        super("Town Data");
        greetingMessages = new HashMap<>();
        farewellMessages = new HashMap<>();
        townBios = new HashMap<>();
        publicSpawnTowns = new HashSet<>();

        regularChunkFlags = new HashMap<>();
        farmChunkFlags = new HashMap<>();
        plotChunkFlags = new HashMap<>();
        reload();
    }

    public void reload() {
        greetingMessages.clear();
        farewellMessages.clear();
        townBios.clear();
        publicSpawnTowns.clear();

        regularChunkFlags.clear();
        farmChunkFlags.clear();
        plotChunkFlags.clear();
        DataManager.updateTownDataCache();
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
        regularChunkFlags.put(newName, regularChunkFlags.remove(oldName));
        farmChunkFlags.put(newName, farmChunkFlags.remove(oldName));
        plotChunkFlags.put(newName, plotChunkFlags.remove(oldName));
    }

    public void disbandReload(String disbandingTown) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        greetingMessages.remove(disbandingTown);
        farewellMessages.remove(disbandingTown);
        townBios.remove(disbandingTown);
        regularChunkFlags.remove(disbandingTown);
        farmChunkFlags.remove(disbandingTown);
        plotChunkFlags.remove(disbandingTown);
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
        regularChunkFlags.put(town, new HashSet<>());
        farmChunkFlags.put(town, new HashSet<>());
        plotChunkFlags.put(town, new HashSet<>());
        for (ClaimedChunk.ChunkType type : flags.keySet()) {
            switch (type) {
                case REGULAR:
                    for (Flag flag : flags.get(type)) {
                        regularChunkFlags.get(town).add(flag);
                        incrementItemsLoaded();
                    }
                    break;
                case FARM:
                    for (Flag flag : flags.get(type)) {
                        farmChunkFlags.get(town).add(flag);
                        incrementItemsLoaded();
                    }
                    break;
                case PLOT:
                    for (Flag flag : flags.get(type)) {
                        plotChunkFlags.get(town).add(flag);
                        incrementItemsLoaded();
                    }
                    break;
            }
        }
    }

    public void setFlag(String town, ClaimedChunk.ChunkType chunkType, Flag newFlag) {
        final HashSet<Flag> flags = getFlags(town, chunkType);
        final HashSet<Flag> updatedFlags = new HashSet<>();
        for (Flag flag : flags) {
            if (!flag.getIdentifier().equalsIgnoreCase(newFlag.getIdentifier())) {
                updatedFlags.add(flag);
            }
        }
        switch (chunkType) {
            case REGULAR -> regularChunkFlags.put(town, updatedFlags);
            case FARM -> farmChunkFlags.put(town, updatedFlags);
            case PLOT -> plotChunkFlags.put(town, updatedFlags);
        }
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

    public HashSet<Flag> getFlags(String town, ClaimedChunk.ChunkType chunkType) {
        return switch (chunkType) {
            case REGULAR -> regularChunkFlags.get(town);
            case FARM -> farmChunkFlags.get(town);
            case PLOT -> plotChunkFlags.get(town);
        };
    }

}
