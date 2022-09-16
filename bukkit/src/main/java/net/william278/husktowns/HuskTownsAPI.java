package net.william278.husktowns;

import net.william278.husktowns.cache.*;
import net.william278.husktowns.flags.Flag;
import net.william278.husktowns.listener.ActionType;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.util.AccessManager;
import net.william278.husktowns.data.DataManager;
import net.william278.husktowns.chunk.ClaimedChunk;
import net.william278.husktowns.town.TownBonus;
import net.william278.husktowns.town.TownRole;
import net.william278.husktowns.util.TownLimitsUtil;
import net.william278.husktowns.cache.ClaimCache;
import net.william278.husktowns.cache.PlayerCache;
import net.william278.husktowns.cache.TownDataCache;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;

/**
 * API methods for HuskTowns
 * <p>
 * Documentation for the API, including a list of methods and example usages can be found here:
 * <a href="https://github.com/WiIIiam278/HuskTowns/wiki/API">https://github.com/WiIIiam278/HuskTowns/wiki/API</a>
 * To use the API, get an instance of it with: {@code HuskTownsAPI api = HuskTownsAPI.getInstance();}
 * Then you can use the various methods provided: {@code api.getTownAt(location);}
 */
@SuppressWarnings("unused")
public class HuskTownsAPI {

    private HuskTownsAPI() {
    }

    private static HuskTownsAPI instance;

    /**
     * Get a new instance of the {@link HuskTownsAPI}.
     *
     * @return instance of the {@link HuskTownsAPI}.
     */
    public static HuskTownsAPI getInstance() {
        if (instance == null) {
            instance = new HuskTownsAPI();
        }
        return instance;
    }

    /* Thread safe methods!
     * The following methods draw data from caches and are safe to be performed on the main thread.*/

    /**
     * Check if the specified {@link Block} is in the wilderness (outside a claim).
     *
     * @param block {@link Block} to check.
     * @return {@code true} if the {@link Block} is in the wilderness; otherwise return {@code false}.
     */
    public boolean isWilderness(Block block) {
        return isWilderness(block.getLocation());
    }

    /**
     * Check if the specified {@link Location} is in the wilderness (outside of a claim).
     *
     * @param location {@link Location} to check.
     * @return {@code true} if the {@link Location} is in the wilderness; otherwise return {@code false}.
     */
    public boolean isWilderness(Location location) {
        ClaimCache cache = HuskTowns.getClaimCache();
        if (cache.getStatus() == CacheStatus.LOADED) {
            return cache.getChunkAt(location.getChunk().getX(),
                    location.getChunk().getZ(), location.getWorld().getName()) == null;
        } else {
            return true;
        }
    }

    /**
     * Returns the name of the town at the specified {@link Location}.
     *
     * @param location {@link Location} to check.
     * @return the name of the town who has a claim at the specified {@link Location}; {@code null} if there is no claim there.
     */
    public String getTownAt(Location location) {
        return getClaimedChunk(location).getTown();
    }

    /**
     * Returns the {@link ClaimedChunk} at the specified {@link Location}; returns null if there is no claim there
     *
     * @param location {@link Location} to check.
     * @return the {@link ClaimedChunk} at the specified position; {@code null} if there's no claim there
     */
    public ClaimedChunk getClaimedChunk(Location location) {
        final int chunkX = location.getChunk().getX();
        final int chunkZ = location.getChunk().getZ();
        final World world = location.getWorld();
        final ClaimCache cache = HuskTowns.getClaimCache();
        if (cache.getStatus() == CacheStatus.LOADED) {
            return cache.getChunkAt(chunkX, chunkZ, world.getName());
        } else {
            return null;
        }
    }

    /**
     * Returns {@code true} if the chunk at the specified {@link Location} is claimed; otherwise returns {@code false}.
     *
     * @param location {@link Location} to check.
     * @return {@code true} if the chunk at {@link Location} is claimed; {@code false} otherwise.
     */
    public boolean isClaimed(Location location) {
        return !isWilderness(location);
    }

    /**
     * Returns the {@link TownRole} of the specified {@link Player} given by their {@link UUID}; null if they are not in a town.
     *
     * @param playerUUID the {@link UUID} to check.
     * @return the {@link TownRole} of the {@link Player} given by their {@link UUID}, or null if they are not in a town.
     */
    public TownRole getPlayerTownRole(UUID playerUUID) {
        PlayerCache cache = HuskTowns.getPlayerCache();
        if (cache.getStatus() == CacheStatus.LOADED) {
            return cache.getPlayerRole(playerUUID);
        } else {
            return null;
        }
    }

    /**
     * Returns the {@link TownRole} of the specified {@link Player}; null if they are not in a town.
     *
     * @param player the {@link Player} to check.
     * @return the {@link TownRole} of the {@link Player}, or null if they are not in a town.
     */
    public TownRole getPlayerTownRole(Player player) {
        return getPlayerTownRole(player.getUniqueId());
    }

    /**
     * Returns the name of the town the {@link Player} is currently in; null if they are not in a town
     *
     * @param player {@link Player} to check.
     * @return the name of the town the {@link Player} is currently in; null if they are not in a town.
     */
    public String getPlayerTown(Player player) {
        return getPlayerTown(player.getUniqueId());
    }

    /**
     * Returns {@code true} if the {@link Player} is in a town; {@code false} if not.
     *
     * @param player {@link Player} to check.
     * @return {@code true} if the {@link Player} is in a town; {@code false} otherwise.
     */
    public boolean isInTown(Player player) {
        return getPlayerTown(player) != null;
    }

    /**
     * Returns the name of the town the {@link Player} given by their {@link UUID} is currently in; null if they are not in a town
     *
     * @param playerUUID {@link UUID} of the {@link Player} to check.
     * @return the name of the town the {@link Player} is currently in; null if they are not in a town.
     */
    public String getPlayerTown(UUID playerUUID) {
        PlayerCache playerCache = HuskTowns.getPlayerCache();
        if (playerCache.getStatus() == CacheStatus.LOADED) {
            return playerCache.getPlayerTown(playerUUID);
        } else {
            return null;
        }
    }

    /**
     * Returns whether the {@link Player} is currently standing in a {@link ClaimedChunk} owned by the town they are in.
     *
     * @param player {@link Player} to check.
     * @return {@code true} if the {@link Player} is standing in a {@link ClaimedChunk} owned by the town they are in; {@code false} otherwise or if they are not in a town
     */
    public boolean isStandingInTown(Player player) {
        if (!isInTown(player)) {
            return false;
        }
        return isLocationClaimedByTown(player.getLocation(), getPlayerTown(player));
    }

    /**
     * Returns whether the location is claimed by a town.
     *
     * @param location {@link Location} to check.
     * @param townName The name of the town to check.
     * @return {@code true} if the location is within a claimed chunk.
     */
    public boolean isLocationClaimedByTown(Location location, String townName) {
        return getClaimedChunk(location).getTown().equals(townName);
    }

    /**
     * Returns whether the action ({@link ActionType}) is allowed to be carried out at the specified {@link Location}
     * Use {@code canPerformAction()}, {@code canBuild()}, {@code canInteract()}, {@code canOpenContainers()}, etc. if you want to check if a {@link Player} is can perform an action.
     *
     * @param location   The {@link Location} to check if the action is allowed to be carried out at.
     * @param actionType The {@link ActionType} to check.
     * @return {@code true} if the action is allowed to occur, {@code false} otherwise.
     */
    public boolean isActionAllowed(Location location, ActionType actionType) {
        return Flag.isActionAllowed(location, actionType);
    }

    /**
     * Returns whether the {@link Player} can perform the action ({@link ActionType}) at the specified {@link Location}.
     *
     * @param player     The {@link Player} performing the action.
     * @param location   The {@link Location} to check if the action can be performed at.
     * @param actionType The {@link ActionType} to check.
     * @return {@code true} if the player is allowed to perform the action, {@code false} otherwise.
     */
    public boolean canPerformAction(Player player, Location location, ActionType actionType) {
        return canPerformAction(player.getUniqueId(), location, actionType);
    }

    /**
     * Returns whether the specified {@link Player} can build at the specified {@link Location}.
     *
     * @param player   {@link Player} to check.
     * @param location {@link Location} to check.
     * @return {@code true} if the player can build at the specified {@link Location}; {@code false} otherwise.
     */
    public boolean canBuild(Player player, Location location) {
        return canBuild(player.getUniqueId(), location);
    }

    /**
     * Returns whether the specified {@link Player} can open containers (e.g {@link org.bukkit.block.Chest}, {@link org.bukkit.block.Barrel}, {@link org.bukkit.block.ShulkerBox}, {@link org.bukkit.block.Hopper}, etc) at the specified {@link Location}.
     *
     * @param player   {@link Player} to check.
     * @param location {@link Location} to check.
     * @return {@code true} if the player can open containers at the specified {@link Location}; {@code false} otherwise.
     */
    public boolean canOpenContainers(Player player, Location location) {
        return canOpenContainers(player.getUniqueId(), location);
    }

    /**
     * Returns whether the specified {@link Player} can interact (push buttons, open doors, use minecarts) - but not necessarily open containers - at the specified {@link Location}.
     *
     * @param player   {@link Player} to check.
     * @param location {@link Location} to check.
     * @return {@code true} if the player can interact at the specified {@link Location}; {@code false} otherwise.
     */
    public boolean canInteract(Player player, Location location) {
        return canInteract(player.getUniqueId(), location);
    }

    /**
     * Returns whether the player specified by their {@link UUID} can perform the action ({@link ActionType}) at the specified {@link Location}.
     *
     * @param uuid       The player who is performing the action's {@link UUID}.
     * @param location   The {@link Location} to check if the action can be performed at.
     * @param actionType The {@link ActionType} to check.
     * @return {@code true} if the player is allowed to perform the action, {@code false} otherwise.
     */
    public boolean canPerformAction(UUID uuid, Location location, ActionType actionType) {
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
        return switch (AccessManager.getPlayerAccess(uuid, actionType, getClaimedChunk(location), true)) {
            case CAN_PERFORM_ACTION_TRUSTED_ACCESS, CAN_PERFORM_ACTION_TOWN_FARM, CAN_PERFORM_ACTION_PLOT_MEMBER, CAN_PERFORM_ACTION_PLOT_OWNER, CAN_PERFORM_ACTION_IGNORING_CLAIMS, CAN_PERFORM_ACTION_ADMIN_CLAIM_ACCESS, CAN_PERFORM_ACTION_PUBLIC_BUILD_ACCESS_FLAG ->
                    true;
            default -> false;
        };
    }

    /**
     * Returns whether the player specified by their {@link UUID} can build at the specified {@link Location}.
     *
     * @param uuid     {@link UUID} of the player to check.
     * @param location {@link Location} to check.
     * @return {@code true} if the player can build at the specified {@link Location}; {@code false} otherwise.
     */
    public boolean canBuild(UUID uuid, Location location) {
        return canPerformAction(uuid, location, ActionType.PLACE_BLOCK);
    }

    /**
     * Returns whether the player specified by their {@link UUID} can open containers (e.g {@link org.bukkit.block.Chest}, {@link org.bukkit.block.Barrel}, {@link org.bukkit.block.ShulkerBox}, {@link org.bukkit.block.Hopper}, etc) at the specified {@link Location}.
     *
     * @param uuid     {@link UUID} of the player to check.
     * @param location {@link Location} to check.
     * @return {@code true} if the player can open containers at the specified {@link Location}; {@code false} otherwise.
     */
    public boolean canOpenContainers(UUID uuid, Location location) {
        return canPerformAction(uuid, location, ActionType.OPEN_CONTAINER);
    }

    /**
     * Returns whether the player specified by their {@link UUID} can interact (push buttons, open doors, use minecarts) - but not necessarily open containers - at the specified {@link Location}.
     *
     * @param uuid     {@link UUID} of the player to check.
     * @param location {@link Location} to check.
     * @return {@code true} if the player can interact at the specified {@link Location}; {@code false} otherwise.
     */
    public boolean canInteract(UUID uuid, Location location) {
        return canPerformAction(uuid, location, ActionType.INTERACT_BLOCKS);
    }

    /**
     * Returns a HashSet of all the usernames of members of a given Town.
     *
     * @param townName the name of the Town.
     * @return the usernames of the town's members.
     */
    public HashSet<String> getPlayersInTown(String townName) {
        PlayerCache cache = HuskTowns.getPlayerCache();
        if (cache.getStatus() == CacheStatus.LOADED) {
            return cache.getPlayersInTown(townName);
        } else {
            return new HashSet<>();
        }
    }

    /**
     * Returns a HashMap of all the members of a given Town and their roles within the town.
     *
     * @param townName The name of the Town.
     * @return the usernames of the town's members and their roles.
     */
    public HashMap<String, TownRole> getPlayersInTownRoles(String townName) {
        HashMap<String, TownRole> playersInTownRoles = new HashMap<>();
        PlayerCache cache = HuskTowns.getPlayerCache();
        if (cache.getStatus() == CacheStatus.LOADED) {
            for (String username : getPlayersInTown(townName)) {
                playersInTownRoles.put(username, cache.getPlayerRole(cache.getUUID(username)));
            }
            return playersInTownRoles;
        } else {
            return new HashMap<>();
        }
    }

    /**
     * Returns the username of the Mayor of the given town name.
     *
     * @param townName The name of the Town.
     * @return the username of the Town's mayor.
     */
    public String getTownMayor(String townName) {
        HashMap<String, TownRole> playersInTownRoles = getPlayersInTownRoles(townName);
        for (String username : playersInTownRoles.keySet()) {
            if (playersInTownRoles.get(username) == TownRole.getMayorRole()) {
                return username;
            }
        }
        return null;
    }

    /**
     * Add a town bonus
     *
     * @param townName     The name of the town to apply a bonus to.
     * @param bonusClaims  The number of additional claims you wish to apply.
     * @param bonusMembers The number of additional members you wish to apply.
     */
    public void addTownBonus(String townName, int bonusClaims, int bonusMembers) {
        final TownBonus bonus = new TownBonus(null, bonusClaims, bonusMembers, Instant.now().getEpochSecond());
        DataManager.addTownBonus(Bukkit.getConsoleSender(), townName, bonus);
    }

    /**
     * Returns the message sent to players when they enter a town's claim.
     *
     * @param townName The name of the town.
     * @return The town's greeting message, {@code null} if the Town Data cache has not loaded.
     */
    public String getTownGreetingMessage(String townName) {
        final TownDataCache cache = HuskTowns.getTownDataCache();
        if (cache.hasLoaded()) {
            return cache.getGreetingMessage(townName);
        }
        return null;
    }

    /**
     * Returns the message sent to players when they leave a town's claim.
     *
     * @param townName The name of the town.
     * @return The town's farewell message, {@code null} if the Town Data cache has not loaded.
     */
    public String getTownFarewellMessage(String townName) {
        final TownDataCache cache = HuskTowns.getTownDataCache();
        if (cache.hasLoaded()) {
            return cache.getFarewellMessage(townName);
        }
        return null;
    }

    /**
     * Returns the bio of a town.
     *
     * @param townName The name of the town.
     * @return The town's bio, {@code null} if the Town Data cache has not loaded.
     */
    public String getTownBio(String townName) {
        final TownDataCache cache = HuskTowns.getTownDataCache();
        if (cache.hasLoaded()) {
            return cache.getTownBio(townName);
        }
        return null;
    }

    /**
     * Get a list of the names of all towns.
     *
     * @return A HashSet of all town names, {@code null} if the Player cache has not loaded.
     */
    public HashSet<String> getTowns() {
        if (isPlayerCacheLoaded()) {
            return HuskTowns.getPlayerCache().getTowns();
        }
        return null;
    }

    /**
     * Get a list of the names of all towns who have their town spawn set to public.
     *
     * @return A HashSet of the names of all towns with their spawn set to public, {@code null} if the Town Data cache has not loaded.
     */
    public HashSet<String> getTownsWithPublicSpawns() {
        if (isTownDataCacheLoaded()) {
            return HuskTowns.getTownDataCache().getPublicSpawnTowns();
        }
        return null;
    }

    /**
     * Get a {@link Player}'s username by their {@link UUID} from the cache.
     *
     * @param uuid the player's {@link UUID}.
     * @return the player's username.
     */
    public String getPlayerUsername(UUID uuid) {
        if (isPlayerCacheLoaded()) {
            return HuskTowns.getPlayerCache().getPlayerUsername(uuid);
        }
        return null;
    }

    /**
     * Returns if the claim cache is loaded.
     *
     * @return {@code true} if the cache is loaded.
     */
    public boolean isClaimCacheLoaded() {
        return HuskTowns.getClaimCache().hasLoaded();
    }

    /**
     * Returns the {@link CacheStatus} of the claim cache.
     *
     * @return The {@link CacheStatus}.
     */
    public CacheStatus getClaimCacheStatus() {
        return HuskTowns.getClaimCache().getStatus();
    }

    /**
     * Returns if the player cache is loaded.
     *
     * @return {@code true} if the cache is loaded.
     */
    public boolean isPlayerCacheLoaded() {
        return HuskTowns.getPlayerCache().hasLoaded();
    }

    /**
     * Returns the {@link CacheStatus} of the player cache.
     *
     * @return The {@link CacheStatus}.
     */
    public CacheStatus getPlayerCacheStatus() {
        return HuskTowns.getPlayerCache().getStatus();
    }


    /**
     * Returns if the town data cache is loaded.
     *
     * @return {@code true} if the cache is loaded.
     */
    public boolean isTownDataCacheLoaded() {
        return HuskTowns.getTownDataCache().hasLoaded();
    }

    /**
     * Returns the {@link CacheStatus} of the town data cache.
     *
     * @return The {@link CacheStatus}.
     */
    public CacheStatus getTownDataCacheStatus() {
        return HuskTowns.getTownDataCache().getStatus();
    }

    /**
     * Returns if the town bonuses cache is loaded.
     *
     * @return {@code true} if the cache is loaded.
     */
    public boolean isTownBonusCacheLoaded() {
        return HuskTowns.getTownBonusesCache().hasLoaded();
    }

    /**
     * Returns the {@link CacheStatus} of the town bonus cache.
     *
     * @return The {@link CacheStatus}.
     */
    public CacheStatus getTownBonusCacheStatus() {
        return HuskTowns.getTownBonusesCache().getStatus();
    }

    /**
     * Returns the hexadecimal color code for a town given its name
     *
     * @param townName The name of the town to get the color of
     * @return The town's color code string (e.g #ffffff)
     */
    public String getTownColorHex(String townName) {
        return Town.getTownColorHex(townName);
    }

    /**
     * Returns the {@link Color} of a town given its name.
     *
     * @param townName The name of the town to get the color of
     * @return The town's {@link Color}
     */
    public Color getTownColor(String townName) {
        return Town.getTownColor(townName);
    }

    /**
     * Returns an unformatted message by ID from the user's messages file
     *
     * @param messageId The ID of the message to fetch
     * @return The message from the player's messages yaml file
     */
    public String getMessageString(String messageId) {
        return MessageManager.getRawMessage(messageId);
    }

    /* Non-thread safe methods!
     * The following methods draw data from SQL data directly rather than caches and should not be used on the main thread.*/

    /**
     * NOT THREAD SAFE - Returns the {@link Town} object with the given name from the database
     *
     * @param townName The name of the town
     * @return the {@link Town} object, or {@code null} if it does not exist
     */
    private Town getTownFromDatabase(String townName) {
        try (Connection connection = HuskTowns.getConnection()) {
            return DataManager.getTownFromName(townName, connection);
        } catch (SQLException e) {
            HuskTowns.getInstance().getLogger().log(Level.SEVERE, "An exception occurred pulling data from SQL via the API", e);
        }
        return null;
    }

    /**
     * NOT THREAD SAFE - Returns the balance of the town with the given name
     *
     * @param townName The name of the town
     * @return the balance, or {@code null} if the town does not exist
     */
    public Double getTownBalance(String townName) {
        Town town = getTownFromDatabase(townName);
        if (town != null) {
            return town.getMoneyDeposited();
        }
        return null;
    }

    /**
     * NOT THREAD SAFE - Returns the level of the town with the given name
     *
     * @param townName The name of the town
     * @return the town level, or {@code null} if the town does not exist
     */
    public Integer getTownLevel(String townName) {
        Town town = getTownFromDatabase(townName);
        if (town != null) {
            return town.getLevel();
        }
        return null;
    }

    /**
     * Returns the amount of money required to be deposited for a town to level up
     *
     * @param townName The name of the town
     * @return The amount of money needed to reach the next level; {@code null} if the town does not exist
     */
    public Double getAmountToNextLevel(String townName) {
        Double coffers = getTownBalance(townName);
        if (coffers != null) {
            return TownLimitsUtil.getNextLevelRequired(coffers);
        }
        return null;
    }

    /**
     * NOT THREAD SAFE - Returns the time the town with the given name was founded
     *
     * @param townName The name of the town
     * @return the town's formatted founded timestamp, or {@code null} if the town does not exist
     */
    public String getTownFoundedTime(String townName) {
        Town town = getTownFromDatabase(townName);
        if (town != null) {
            return town.getFormattedFoundedTime();
        }
        return null;
    }

}
