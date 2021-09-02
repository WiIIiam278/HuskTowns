import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.listener.EventListener;
import me.william278.husktowns.object.cache.Cache;
import me.william278.husktowns.object.cache.ClaimCache;
import me.william278.husktowns.object.cache.PlayerCache;
import me.william278.husktowns.object.cache.TownDataCache;
import me.william278.husktowns.object.chunk.ClaimedChunk;
import me.william278.husktowns.object.town.Town;
import me.william278.husktowns.object.town.TownBonus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

/**
 * HuskTowns' API methods class.
 * @author William
 */
public class HuskTownsAPI {

    private static HuskTownsAPI instance;

    /**
     * Get the HuskTowns API
     * @return instance of the HuskTowns API
     */
    public static HuskTownsAPI getInstance() {
        if (instance == null) {
            instance = new HuskTownsAPI();
        }
        return instance;
    }

    /**
     * Check if the specified {@link Block} is in the wilderness (outside of a claim).
     * @param block {@link Block} to check.
     * @return true if the {@link Block} is in the wilderness; otherwise return false.
     */
    public boolean isWilderness(Block block) {
        return isWilderness(block.getLocation());
    }

    /**
     * Check if the specified {@link Location} is in the wilderness (outside of a claim).
     * @param location {@link Location} to check.
     * @return true if the {@link Location} is in the wilderness; otherwise return false.
     */
    public boolean isWilderness(Location location) {
        ClaimCache cache = HuskTowns.getClaimCache();
        if (cache.getStatus() == Cache.CacheStatus.LOADED) {
            return cache.getChunkAt(location.getChunk().getX(),
                    location.getChunk().getZ(), location.getWorld().getName()) == null;
        } else {
            return true;
        }
    }

    /**
     * Returns the name of the town at the specified {@link Location}.
     * @param location {@link Location} to check.
     * @return the name of the town who has a claim at the specified {@link Location}; null if there is no claim there.
     */
    public String getTownAt(Location location) {
        return getClaimedChunk(location).getTown();
    }

    /**
     * Returns the {@link ClaimedChunk} at the specified {@link Location}; returns null if there is no claim there
     * @param location {@link Location} to check.
     * @return the {@link ClaimedChunk} at the specified position or null if there's no claim there
     */
    public ClaimedChunk getClaimedChunk(Location location) {
        int chunkX = location.getChunk().getX();
        int chunkZ = location.getChunk().getZ();
        World world = location.getWorld();
        ClaimCache cache = HuskTowns.getClaimCache();
        if (cache.getStatus() == Cache.CacheStatus.LOADED) {
            return cache.getChunkAt(chunkX, chunkZ, world.getName());
        } else {
            return null;
        }
    }

    /**
     * Returns true if the chunk at the specified {@link Location} is claimed; otherwise returns false.
     * @param location {@link Location} to check.
     * @return true if the chunk at {@link Location} is claimed; false otherwise.
     */
    public boolean isClaimed(Location location) {
        return !isWilderness(location);
    }

    /**
     * Returns the {@link Town.TownRole} of the specified {@link Player} given by their {@link UUID}; null if they are not in a town.
     * @param playerUUID the {@link UUID} to check.
     * @return the {@link Town.TownRole} of the {@link Player} given by their {@link UUID}, or null if they are not in a town.
     */
    public Town.TownRole getPlayerTownRole(UUID playerUUID) {
        PlayerCache cache = HuskTowns.getPlayerCache();
        if (cache.getStatus() == Cache.CacheStatus.LOADED) {
            return cache.getPlayerRole(playerUUID);
        } else {
            return null;
        }
    }

    /**
     * Returns the {@link Town.TownRole} of the specified {@link Player}; null if they are not in a town.
     * @param player the {@link Player} to check.
     * @return the {@link Town.TownRole} of the {@link Player}, or null if they are not in a town.
     */
    public Town.TownRole getPlayerTownRole(Player player) {
        return getPlayerTownRole(player.getUniqueId());
    }

    /**
     * Returns the name of the town the {@link Player} is currently in; null if they are not in a town
     * @param player {@link Player} to check.
     * @return the name of the town the {@link Player} is currently in; null if they are not in a town.
     */
    public String getPlayerTown(Player player) {
        return getPlayerTown(player.getUniqueId());
    }

    /**
     * Returns true if the {@link Player} is in a town; false if not.
     * @param player {@link Player} to check.
     * @return true if the {@link Player} is in a town; false otherwise.
     */
    public boolean isInTown(Player player) {
        return getPlayerTown(player) != null;
    }

    /**
     * Returns the name of the town the {@link Player} given by their {@link UUID} is currently in; null if they are not in a town
     * @param playerUUID {@link UUID} of the {@link Player} to check.
     * @return the name of the town the {@link Player} is currently in; null if they are not in a town.
     */
    public String getPlayerTown(UUID playerUUID) {
        PlayerCache playerCache = HuskTowns.getPlayerCache();
        if (playerCache.getStatus() == Cache.CacheStatus.LOADED) {
            return playerCache.getPlayerTown(playerUUID);
        } else {
            return null;
        }
    }

    /**
     * Returns whether the {@link Player} is currently standing in a {@link ClaimedChunk} owned by the town they are in.
     * @param player {@link Player} to check.
     * @return true if the {@link Player} is standing in a {@link ClaimedChunk} owned by the town they are in; false otherwise or if they are not in a town
     */
    public boolean isStandingInTown(Player player) {
        if (!isInTown(player)) {
            return false;
        }
        return isLocationClaimedByTown(player.getLocation(), getPlayerTown(player));
    }

    public boolean isLocationClaimedByTown(Location location, String townName) {
        return getClaimedChunk(location).getTown().equals(townName);
    }

    /**
     * Returns whether the specified {@link Player} can build at the specified {@link Location}.
     * @param player {@link Player} to check.
     * @param location {@link Location} to check.
     * @return true if the player can build at the specified {@link Location}; false otherwise.
     */
    public boolean canBuild(Player player, Location location) {
        return canBuild(player.getUniqueId(), location);
    }

    /**
     * Returns whether the player specified by their {@link UUID} can build at the specified {@link Location}.
     * @param uuid {@link UUID} of the player to check.
     * @param location {@link Location} to check.
     * @return true if the player can build at the specified {@link Location}; false otherwise.
     */
    public boolean canBuild(UUID uuid, Location location) {
        final ClaimCache claimCache = HuskTowns.getClaimCache();
        if (!claimCache.hasLoaded()) {
            return false;
        }
        final PlayerCache playerCache = HuskTowns.getPlayerCache();
        if (!playerCache.hasLoaded()) {
            return false;
        }

        if (isWilderness(location)) {
            return true;
        }
        if (isLocationClaimedByTown(location, getPlayerTown(uuid))) {
            return switch (getClaimedChunk(location).getPlayerAccess(uuid, EventListener.ActionType.PLACE_BLOCK)) {
                case CAN_BUILD_TRUSTED, CAN_BUILD_TOWN_FARM, CAN_BUILD_PLOT_MEMBER, CAN_BUILD_PLOT_OWNER, CAN_BUILD_IGNORING_CLAIMS, CAN_BUILD_ADMIN_CLAIM_ACCESS, CAN_BUILD_PUBLIC_BUILD_ACCESS_FLAG -> true;
                default -> false;
            };
        } else {
            return false;
        }
    }

    /**
     * Returns a HashSet of all the usernames of members of a given Town
     * @param townName the name of the Town
     * @return the usernames of the town's members
     */
    public HashSet<String> getPlayersInTown(String townName) {
        PlayerCache cache = HuskTowns.getPlayerCache();
        if (cache.getStatus() == Cache.CacheStatus.LOADED) {
            return cache.getPlayersInTown(townName);
        } else {
            return new HashSet<>();
        }
    }

    /**
     * Returns a HashMap of all the members of a given Town and their roles within the town
     * @param townName The name of the Town
     * @return the usernames of the town's members and their roles
     */
    public HashMap<String, Town.TownRole> getPlayersInTownRoles(String townName) {
        HashMap<String, Town.TownRole> playersInTownRoles = new HashMap<>();
        PlayerCache cache = HuskTowns.getPlayerCache();
        if (cache.getStatus() == Cache.CacheStatus.LOADED) {
            for (String username : getPlayersInTown(townName)) {
                playersInTownRoles.put(username, cache.getPlayerRole(cache.getUUID(username)));
            }
            return playersInTownRoles;
        } else {
            return new HashMap<>();
        }
    }

    /**
     * Returns the username of the Mayor of the given Town Name
     * @param townName The name of the Town
     * @return the username of the Town's mayor.
     */
    public String getTownMayor(String townName) {
        HashMap<String, Town.TownRole> playersInTownRoles = getPlayersInTownRoles(townName);
        for (String username : playersInTownRoles.keySet()) {
            if (playersInTownRoles.get(username) == Town.TownRole.MAYOR) {
                return username;
            }
        }
        return null;
    }

    /**
     * Add a town bonus
     * @param townName The name of the {@link me.william278.husktowns.object.town.Town} to apply a bonus to
     * @param bonus The {@link TownBonus} to apply to the town
     */
    public void addTownBonus(String townName, TownBonus bonus) {
        DataManager.addTownBonus(Bukkit.getConsoleSender(), townName, bonus);
    }

    /**
     * Returns the message sent to players when they enter a town's claim
     * @param townName The name of the town
     * @return The town's greeting message.
     */
    public String getTownGreetingMessage(String townName) {
        final TownDataCache cache = HuskTowns.getTownDataCache();
        if (cache.hasLoaded()) {
            return cache.getGreetingMessage(townName);
        }
        return null;
    }

    /**
     * Returns the message sent to players when they leave a town's claim
     * @param townName The name of the town
     * @return The town's farewell message.
     */
    public String getTownFarewellMessage(String townName) {
        final TownDataCache cache = HuskTowns.getTownDataCache();
        if (cache.hasLoaded()) {
            return cache.getFarewellMessage(townName);
        }
        return null;
    }

    /**
     * Returns the bio of a town
     * @param townName The name of the town
     * @return The town's bio.
     */
    public String getTownBio(String townName) {
        final TownDataCache cache = HuskTowns.getTownDataCache();
        if (cache.hasLoaded()) {
            return cache.getTownBio(townName);
        }
        return null;
    }

    /**
     * Get a list of the names of all towns
     * @return A HashSet of all town names
     */
    public HashSet<String> getTowns() {
        if (HuskTowns.getPlayerCache().hasLoaded()) {
            return HuskTowns.getPlayerCache().getTowns();
        }
        return null;
    }

    /**
     * Get a list of the names of all towns who have their town spawn set to public
     * @return A HashSet of the names of all towns with their spawn set to public
     */
    public HashSet<String > getTownsWithPublicSpawns() {
        if (HuskTowns.getPlayerCache().hasLoaded()) {
            return HuskTowns.getTownDataCache().getPublicSpawnTowns();
        }
        return null;
    }

    /**
     * Returns the {@link me.william278.husktowns.object.cache.Cache.CacheStatus} of the claim cache
     * @return the cache's status
     */
    public Cache.CacheStatus getClaimedCacheStatus() {
        return HuskTowns.getClaimCache().getStatus();
    }

    /**
     * Returns the {@link me.william278.husktowns.object.cache.Cache.CacheStatus} of the player cache
     * @return the cache's status
     */
    public Cache.CacheStatus getPlayerCacheStatus() {
        return HuskTowns.getPlayerCache().getStatus();
    }

    /**
     * Returns the {@link me.william278.husktowns.object.cache.Cache.CacheStatus} of the town message cache
     * @return the cache's status
     */
    public Cache.CacheStatus getTownMessageCacheStatus() {
        return HuskTowns.getTownDataCache().getStatus();
    }

    /**
     * Returns the {@link me.william278.husktowns.object.cache.Cache.CacheStatus} of the town bonuses cache
     * @return the cache's status
     */
    public Cache.CacheStatus getTownBonusCacheStatus() {
        return HuskTowns.getTownBonusesCache().getStatus();
    }
}
