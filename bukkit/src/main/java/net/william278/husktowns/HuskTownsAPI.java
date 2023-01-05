package net.william278.husktowns;

import net.william278.husktowns.cache.CacheStatus;
import net.william278.husktowns.chunk.ClaimedChunk;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.listener.ActionType;
import net.william278.husktowns.listener.Operation;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.town.TownRole;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.SavedUser;
import net.william278.husktowns.user.User;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The legacy HuskTowns API, for maintaining compatibility with v1.0 plugins.
 *
 * @deprecated Use the new HuskTowns API@v2 instead
 */
@Deprecated(since = "2.0")
public class HuskTownsAPI {
    private static HuskTownsAPI instance;
    private final BukkitHuskTowns plugin;

    private HuskTownsAPI() {
        this.plugin = BukkitHuskTowns.getInstance();
    }

    /**
     * Get a new instance of the {@link HuskTownsAPI}.
     *
     * @return instance of the {@link HuskTownsAPI}.
     * @deprecated Use the new HuskTowns API@v2 instead
     */
    @NotNull
    @Deprecated(since = "2.0")
    public static HuskTownsAPI getInstance() {
        return instance == null ? instance = new HuskTownsAPI() : instance;
    }

    /**
     * Check if the specified {@link Location} is in the wilderness (outside of a claim).
     *
     * @param location {@link Location} to check.
     * @return {@code true} if the {@link Location} is in the wilderness; otherwise return {@code false}.
     */
    @Deprecated(since = "2.0")
    public boolean isWilderness(@NotNull Location location) {
        return getClaimAt(location).isEmpty();
    }

    /**
     * Check if the specified {@link Block} is in the wilderness (outside a claim).
     *
     * @param block {@link Block} to check.
     * @return {@code true} if the {@link Block} is in the wilderness; otherwise return {@code false}.
     */
    @Deprecated(since = "2.0")
    public boolean isWilderness(@NotNull Block block) {
        return this.isWilderness(block.getLocation());
    }

    /**
     * Returns the name of the town at the specified {@link Location}.
     *
     * @param location {@link Location} to check.
     * @return the name of the town who has a claim at the specified {@link Location}; {@code null} if there is no claim there.
     */
    @Deprecated(since = "2.0")
    public String getTownAt(@NotNull Location location) {
        return getClaimAt(location)
                .map(TownClaim::town)
                .map(Town::getName)
                .orElse(null);
    }

    /**
     * Returns the {@link ClaimedChunk} at the specified {@link Location}; returns null if there is no claim there
     *
     * @param location {@link Location} to check.
     * @return the {@link ClaimedChunk} at the specified position; {@code null} if there's no claim there
     */
    @Deprecated(since = "2.0")
    @Nullable
    public ClaimedChunk getClaimedChunk(@NotNull Location location) {
        assert location.getWorld() != null;
        return getClaimAt(location)
                .map(claim -> ClaimedChunk.fromClaim(claim,
                        World.of(location.getWorld().getUID(), location.getWorld().getName(),
                                location.getWorld().getEnvironment().name().toLowerCase())))
                .orElse(null);
    }

    /**
     * Returns {@code true} if the chunk at the specified {@link Location} is claimed; otherwise returns {@code false}.
     *
     * @param location {@link Location} to check.
     * @return {@code true} if the chunk at {@link Location} is claimed; {@code false} otherwise.
     */
    @Deprecated(since = "2.0")
    public boolean isClaimed(@NotNull Location location) {
        return !this.isWilderness(location);
    }


    // Internal - get a TownClaim at a Bukkit Location
    private Optional<TownClaim> getClaimAt(@NotNull Location location) {
        assert location.getWorld() != null;
        final Position position = Position.at(location.getX(), location.getY(), location.getZ(),
                World.of(location.getWorld().getUID(), location.getWorld().getName(),
                        location.getWorld().getEnvironment().name().toLowerCase()));
        return plugin.getClaimAt(position);
    }

    /**
     * Returns the {@link TownRole} of the specified {@link Player} given by their {@link UUID}; null if they are not in a town.
     *
     * @param playerUUID the {@link UUID} to check.
     * @return the {@link TownRole} of the {@link Player} given by their {@link UUID}, or null if they are not in a town.
     */
    @Deprecated(since = "2.0")
    @Nullable
    public TownRole getPlayerTownRole(@NotNull UUID playerUUID) {
        return getTownMember(playerUUID).map(Member::role).map(TownRole::fromRole)
                .orElse(null);
    }

    /**
     * Returns the {@link TownRole} of the specified {@link Player}; null if they are not in a town.
     *
     * @param player the {@link Player} to check.
     * @return the {@link TownRole} of the {@link Player}, or null if they are not in a town.
     */
    @Deprecated(since = "2.0")
    @Nullable
    public TownRole getPlayerTownRole(@NotNull Player player) {
        return getPlayerTownRole(player.getUniqueId());
    }

    /**
     * Returns the name of the town the {@link Player} is currently in; null if they are not in a town
     *
     * @param player {@link Player} to check.
     * @return the name of the town the {@link Player} is currently in; null if they are not in a town.
     */
    @Deprecated(since = "2.0")
    @Nullable
    public String getPlayerTown(@NotNull Player player) {
        return getTownMember(player.getUniqueId())
                .map(Member::town).map(Town::getName)
                .orElse(null);
    }

    /**
     * Returns {@code true} if the {@link Player} is in a town; {@code false} if not.
     *
     * @param player {@link Player} to check.
     * @return {@code true} if the {@link Player} is in a town; {@code false} otherwise.
     */
    @Deprecated(since = "2.0")
    public boolean isInTown(@NotNull Player player) {
        return plugin.getUserTown(BukkitUser.adapt(player)).isPresent();
    }

    /**
     * Returns the name of the town the {@link Player} given by their {@link UUID} is currently in; null if they are not in a town
     *
     * @param playerUUID {@link UUID} of the {@link Player} to check.
     * @return the name of the town the {@link Player} is currently in; null if they are not in a town.
     */
    @Deprecated(since = "2.0")
    @Nullable
    public String getPlayerTown(@NotNull UUID playerUUID) {
        return getTownMember(playerUUID)
                .map(Member::town).map(Town::getName)
                .orElse(null);
    }

    /**
     * Returns whether the {@link Player} is currently standing in a {@link ClaimedChunk} owned by the town they are in.
     *
     * @param player {@link Player} to check.
     * @return {@code true} if the {@link Player} is standing in a {@link ClaimedChunk} owned by the town they are in; {@code false} otherwise or if they are not in a town
     */
    @Deprecated(since = "2.0")
    public boolean isStandingInTown(@NotNull Player player) {
        final Optional<TownClaim> claim = getClaimAt(player.getLocation());
        if (claim.isEmpty()) {
            return false;
        }
        return claim.map(TownClaim::town)
                .equals(plugin.getUserTown(BukkitUser.adapt(player)).map(Member::town));
    }

    // Internal - get a town member from a player UUID
    private Optional<Member> getTownMember(@NotNull UUID playerUUID) {
        return plugin.getUserTown(User.of(playerUUID, playerUUID.toString().split("-")[0]));
    }

    /**
     * Returns whether the location is claimed by a town.
     *
     * @param location {@link Location} to check.
     * @param townName The name of the town to check.
     * @return {@code true} if the location is within a claimed chunk.
     */
    @Deprecated(since = "2.0")
    public boolean isLocationClaimedByTown(@NotNull Location location, @NotNull String townName) {
        final Optional<TownClaim> claim = getClaimAt(location);
        return claim.isPresent() && claim.get().town().getName().equals(townName);
    }

    /**
     * Returns whether the action ({@link ActionType}) is allowed to be carried out at the specified {@link Location}
     * Use {@code canPerformAction()}, {@code canBuild()}, {@code canInteract()}, {@code canOpenContainers()}, etc. if you want to check if a {@link Player} is can perform an action.
     *
     * @param location   The {@link Location} to check if the action is allowed to be carried out at.
     * @param actionType The {@link ActionType} to check.
     * @return {@code true} if the action is allowed to occur, {@code false} otherwise.
     */
    @Deprecated(since = "2.0")
    public boolean isActionAllowed(@NotNull Location location, @NotNull ActionType actionType) {
        assert location.getWorld() != null;
        return plugin.getOperationHandler().cancelOperation(Operation.of(actionType.getOperationType(),
                Position.at(location.getX(), location.getY(), location.getZ(),
                        World.of(location.getWorld().getUID(), location.getWorld().getName(),
                                location.getWorld().getEnvironment().name().toLowerCase()))));
    }

    /**
     * Returns whether the {@link Player} can perform the action ({@link ActionType}) at the specified {@link Location}.
     *
     * @param player     The {@link Player} performing the action.
     * @param location   The {@link Location} to check if the action can be performed at.
     * @param actionType The {@link ActionType} to check.
     * @return {@code true} if the player is allowed to perform the action, {@code false} otherwise.
     */
    @Deprecated(since = "2.0")
    public boolean canPerformAction(@NotNull Player player, @NotNull Location location, @NotNull ActionType actionType) {
        return canPerformAction(player.getUniqueId(), location, actionType);
    }

    /**
     * Returns whether the specified {@link Player} can build at the specified {@link Location}.
     *
     * @param player   {@link Player} to check.
     * @param location {@link Location} to check.
     * @return {@code true} if the player can build at the specified {@link Location}; {@code false} otherwise.
     */
    @Deprecated(since = "2.0")
    public boolean canBuild(@NotNull Player player, @NotNull Location location) {
        return canBuild(player.getUniqueId(), location);
    }

    /**
     * Returns whether the specified {@link Player} can open containers (e.g {@link org.bukkit.block.Chest}, {@link org.bukkit.block.Barrel}, {@link org.bukkit.block.ShulkerBox}, {@link org.bukkit.block.Hopper}, etc) at the specified {@link Location}.
     *
     * @param player   {@link Player} to check.
     * @param location {@link Location} to check.
     * @return {@code true} if the player can open containers at the specified {@link Location}; {@code false} otherwise.
     */
    @Deprecated(since = "2.0")
    public boolean canOpenContainers(@NotNull Player player, @NotNull Location location) {
        return canOpenContainers(player.getUniqueId(), location);
    }

    /**
     * Returns whether the specified {@link Player} can interact (push buttons, open doors, use minecarts) - but not necessarily open containers - at the specified {@link Location}.
     *
     * @param player   {@link Player} to check.
     * @param location {@link Location} to check.
     * @return {@code true} if the player can interact at the specified {@link Location}; {@code false} otherwise.
     */
    @Deprecated(since = "2.0")
    public boolean canInteract(@NotNull Player player, @NotNull Location location) {
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
    @Deprecated(since = "2.0")
    public boolean canPerformAction(@NotNull UUID uuid, @NotNull Location location, @NotNull ActionType actionType) {
        assert location.getWorld() != null;
        final Optional<? extends OnlineUser> user = plugin.getOnlineUsers().stream()
                .filter(online -> online.getUuid().equals(uuid)).findFirst();
        final Position position = Position.at(location.getX(), location.getY(), location.getZ(),
                World.of(location.getWorld().getUID(), location.getWorld().getName(),
                        location.getWorld().getEnvironment().name().toLowerCase()));
        return user.map(onlineUser -> plugin.getOperationHandler()
                        .cancelOperation(Operation.of(onlineUser, actionType.getOperationType(), position)))
                .orElseGet(() -> isActionAllowed(location, actionType));
    }

    /**
     * Returns whether the player specified by their {@link UUID} can build at the specified {@link Location}.
     *
     * @param uuid     {@link UUID} of the player to check.
     * @param location {@link Location} to check.
     * @return {@code true} if the player can build at the specified {@link Location}; {@code false} otherwise.
     */
    @Deprecated(since = "2.0")
    public boolean canBuild(@NotNull UUID uuid, @NotNull Location location) {
        return canPerformAction(uuid, location, ActionType.PLACE_BLOCK);
    }

    /**
     * Returns whether the player specified by their {@link UUID} can open containers (e.g {@link org.bukkit.block.Chest}, {@link org.bukkit.block.Barrel}, {@link org.bukkit.block.ShulkerBox}, {@link org.bukkit.block.Hopper}, etc) at the specified {@link Location}.
     *
     * @param uuid     {@link UUID} of the player to check.
     * @param location {@link Location} to check.
     * @return {@code true} if the player can open containers at the specified {@link Location}; {@code false} otherwise.
     */
    @Deprecated(since = "2.0")
    public boolean canOpenContainers(@NotNull UUID uuid, @NotNull Location location) {
        return canPerformAction(uuid, location, ActionType.OPEN_CONTAINER);
    }

    /**
     * Returns whether the player specified by their {@link UUID} can interact (push buttons, open doors, use minecarts) - but not necessarily open containers - at the specified {@link Location}.
     *
     * @param uuid     {@link UUID} of the player to check.
     * @param location {@link Location} to check.
     * @return {@code true} if the player can interact at the specified {@link Location}; {@code false} otherwise.
     */
    @Deprecated(since = "2.0")
    public boolean canInteract(@NotNull UUID uuid, @NotNull Location location) {
        return canPerformAction(uuid, location, ActionType.INTERACT_BLOCKS);
    }

    /**
     * Returns a HashSet of all the usernames of members of a given Town.
     *
     * @param townName the name of the Town.
     * @return the usernames of the town's members.
     */
    @Deprecated(since = "2.0")
    @NotNull
    public HashSet<String> getPlayersInTown(@NotNull String townName) {
        final Optional<Town> optionalTown = plugin.getTowns().stream()
                .filter(town -> town.getName().equalsIgnoreCase(townName)).findFirst();
        if (optionalTown.isEmpty()) {
            return new HashSet<>();
        }
        final Town town = optionalTown.get();
        final HashSet<String> usernames = new HashSet<>();
        for (final UUID uuid : town.getMembers().keySet()) {
            plugin.getDatabase().getUser(uuid).ifPresent(user -> usernames.add(user.user().getUsername()));
        }
        return usernames;
    }

    /**
     * Returns a HashMap of all the members of a given Town and their roles within the town.
     *
     * @param townName The name of the Town.
     * @return the usernames of the town's members and their roles.
     */
    @Deprecated(since = "2.0")
    @NotNull
    public HashMap<String, TownRole> getPlayersInTownRoles(@NotNull String townName) {
        final Optional<Town> optionalTown = plugin.getTowns().stream()
                .filter(town -> town.getName().equalsIgnoreCase(townName)).findFirst();
        if (optionalTown.isEmpty()) {
            return new HashMap<>();
        }
        final Town town = optionalTown.get();
        final HashMap<String, TownRole> roles = new HashMap<>();
        for (final UUID uuid : town.getMembers().keySet()) {
            plugin.getRoles().fromWeight(town.getMembers().get(uuid)).map(TownRole::fromRole)
                    .ifPresent(role -> plugin.getDatabase().getUser(uuid)
                            .ifPresent(user -> roles.put(user.user().getUsername(), role)));
        }
        return roles;
    }

    /**
     * Returns the username of the Mayor of the given town name.
     *
     * @param townName The name of the Town.
     * @return the username of the Town's mayor.
     */
    @Deprecated(since = "2.0")
    @Nullable
    public String getTownMayor(String townName) {
        return plugin.getTowns().stream()
                .filter(town -> town.getName().equalsIgnoreCase(townName)).findFirst()
                .flatMap(town -> plugin.getDatabase().getUser(town.getMayor())
                        .map(SavedUser::user).map(User::getUsername))
                .orElse(null);
    }

    /**
     * Add a town bonus
     *
     * @param townName     The name of the town to apply a bonus to.
     * @param bonusClaims  The number of additional claims you wish to apply.
     * @param bonusMembers The number of additional members you wish to apply.
     */
    @Deprecated(since = "2.0")
    public void addTownBonus(String townName, int bonusClaims, int bonusMembers) {
        plugin.runAsync(() -> plugin.getTowns().stream()
                .filter(town -> town.getName().equalsIgnoreCase(townName)).findFirst()
                .ifPresent(town -> {
                    town.setBonusClaims(town.getBonusClaims() + Math.max(bonusClaims, 0));
                    town.setBonusMembers(town.getBonusMembers() + Math.max(bonusMembers, 0));
                    plugin.getOnlineUsers().stream().findAny().ifPresentOrElse(
                            updater -> plugin.getManager().updateTown(updater, town),
                            () -> {
                                plugin.getTowns().replaceAll(t -> t.getName()
                                        .equalsIgnoreCase(town.getName()) ? town : t);
                                plugin.getDatabase().updateTown(town);
                            });
                }));
    }

    /**
     * Returns the message sent to players when they enter a town's claim.
     *
     * @param townName The name of the town.
     * @return The town's greeting message, {@code null} if the Town Data cache has not loaded.
     */
    @Deprecated(since = "2.0")
    @Nullable
    public String getTownGreetingMessage(@NotNull String townName) {
        return findTownByName(townName)
                .flatMap(Town::getGreeting).orElse(null);
    }

    /**
     * Returns the message sent to players when they leave a town's claim.
     *
     * @param townName The name of the town.
     * @return The town's farewell message, {@code null} if the Town Data cache has not loaded.
     */
    @Deprecated(since = "2.0")
    @Nullable
    public String getTownFarewellMessage(@NotNull String townName) {
        return findTownByName(townName)
                .flatMap(Town::getFarewell).orElse(null);
    }

    /**
     * Returns the bio of a town.
     *
     * @param townName The name of the town.
     * @return The town's bio, {@code null} if the Town Data cache has not loaded.
     */
    @Deprecated(since = "2.0")
    @Nullable
    public String getTownBio(@NotNull String townName) {
        return findTownByName(townName).flatMap(Town::getBio)
                .orElse(null);
    }

    /**
     * Get a list of the names of all towns.
     *
     * @return A HashSet of all town names, {@code null} if the Player cache has not loaded.
     */
    @Deprecated(since = "2.0")
    @NotNull
    public HashSet<String> getTowns() {
        return plugin.getTowns().stream().map(Town::getName)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Get a list of the names of all towns who have their town spawn set to public.
     *
     * @return A HashSet of the names of all towns with their spawn set to public,
     * {@code null} if the Town Data cache has not loaded.
     */
    @Deprecated(since = "2.0")
    @NotNull
    public HashSet<String> getTownsWithPublicSpawns() {
        return plugin.getTowns().stream()
                .filter(town -> town.getSpawn().isPresent() && town.getSpawn().get().isPublic())
                .map(Town::getName)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Get a {@link Player}'s username by their {@link UUID} from the cache.
     *
     * @param uuid the player's {@link UUID}.
     * @return the player's username.
     */
    @Deprecated(since = "2.0")
    @Nullable
    public String getPlayerUsername(@NotNull UUID uuid) {
        return plugin.getOnlineUsers().stream().filter(user -> user.getUuid().equals(uuid))
                .map(OnlineUser::getUsername).findFirst()
                .orElse(plugin.getDatabase().getUser(uuid).map(SavedUser::user)
                        .map(User::getUsername)
                        .orElse(null));
    }

    /**
     * Returns if the claim cache is loaded.
     *
     * @return {@code true} if the cache is loaded.
     */
    @Deprecated(since = "2.0")
    public boolean isClaimCacheLoaded() {
        return plugin.isLoaded();
    }

    /**
     * Returns the {@link CacheStatus} of the claim cache.
     *
     * @return The {@link CacheStatus}.
     * @deprecated See {@link HuskTowns#isLoaded()}. This will only return {@link CacheStatus#LOADED} if the plugin
     * has loaded, or {@link CacheStatus#UNINITIALIZED} if not.
     */
    @Deprecated(since = "2.0")
    @NotNull
    public CacheStatus getClaimCacheStatus() {
        return plugin.isLoaded() ? CacheStatus.LOADED : CacheStatus.UNINITIALIZED;
    }

    /**
     * Returns if the player cache is loaded.
     *
     * @return {@code true} if the cache is loaded.
     */
    @Deprecated(since = "2.0")
    public boolean isPlayerCacheLoaded() {
        return plugin.isLoaded();
    }

    /**
     * Returns the {@link CacheStatus} of the player cache.
     *
     * @return The {@link CacheStatus}.
     * @deprecated This will only return {@link CacheStatus#LOADED} if the plugin has loaded,
     * or {@link CacheStatus#UNINITIALIZED} if not.
     */
    @Deprecated(since = "2.0")
    @NotNull
    public CacheStatus getPlayerCacheStatus() {
        return plugin.isLoaded() ? CacheStatus.LOADED : CacheStatus.UNINITIALIZED;
    }


    /**
     * Returns if the town data cache is loaded.
     *
     * @return {@code true} if the cache is loaded.
     */
    @Deprecated(since = "2.0")
    public boolean isTownDataCacheLoaded() {
        return plugin.isLoaded();
    }

    /**
     * Returns the {@link CacheStatus} of the town bonus cache.
     *
     * @return The {@link CacheStatus}.
     * @deprecated This will only return {@link CacheStatus#LOADED} if the plugin has loaded,
     * or {@link CacheStatus#UNINITIALIZED} if not.
     */
    @Deprecated(since = "2.0")
    @NotNull
    public CacheStatus getTownDataCacheStatus() {
        return plugin.isLoaded() ? CacheStatus.LOADED : CacheStatus.UNINITIALIZED;
    }

    /**
     * Returns if the town bonuses cache is loaded.
     *
     * @return {@code true} if the cache is loaded.
     */
    @Deprecated(since = "2.0")
    public boolean isTownBonusCacheLoaded() {
        return plugin.isLoaded();
    }

    /**
     * Returns the {@link CacheStatus} of the town bonus cache.
     *
     * @return The {@link CacheStatus}.
     * @deprecated This will only return {@link CacheStatus#LOADED} if the plugin has loaded,
     * or {@link CacheStatus#UNINITIALIZED} if not.
     */
    @Deprecated(since = "2.0")
    @NotNull
    public CacheStatus getTownBonusCacheStatus() {
        return plugin.isLoaded() ? CacheStatus.LOADED : CacheStatus.UNINITIALIZED;
    }

    /**
     * Returns the hexadecimal color code for a town given its name
     *
     * @param townName The name of the town to get the color of
     * @return The town's color code string (e.g #ffffff)
     */
    @Deprecated(since = "2.0")
    @Nullable
    public String getTownColorHex(String townName) {
        return findTownByName(townName).map(Town::getColorRgb)
                .orElse(null);
    }

    /**
     * Returns the {@link Color} of a town given its name.
     *
     * @param townName The name of the town to get the color of
     * @return The town's {@link Color}
     */
    @Deprecated(since = "2.0")
    @Nullable
    public Color getTownColor(String townName) {
        return findTownByName(townName).map(Town::getColor)
                .orElse(null);
    }

    /**
     * Returns an unformatted message by ID from the user's messages file
     *
     * @param messageId The ID of the message to fetch
     * @return The message from the player's messages yaml file
     */
    @Deprecated(since = "2.0")
    @Nullable
    public String getMessageString(@NotNull String messageId) {
        return plugin.getLocales().getRawLocale(messageId).orElse(null);
    }

    /**
     * Returns the balance of the town with the given name
     *
     * @param townName The name of the town
     * @return the balance, or {@code null} if the town does not exist
     */
    @Deprecated(since = "2.0")
    @Nullable
    public Double getTownBalance(@NotNull String townName) {
        return findTownByName(townName).map(Town::getMoney).map(BigDecimal::doubleValue)
                .orElse(null);
    }

    /**
     * Returns the level of the town with the given name
     *
     * @param townName The name of the town
     * @return the town level, or {@code null} if the town does not exist
     */
    @Deprecated(since = "2.0")
    @Nullable
    public Integer getTownLevel(@NotNull String townName) {
        return findTownByName(townName).map(Town::getLevel)
                .orElse(null);
    }

    /**
     * Returns the time the town with the given name was founded
     *
     * @param townName The name of the town
     * @return the town's formatted founded timestamp, or {@code null} if the town does not exist
     */
    @Deprecated(since = "2.0")
    @Nullable
    public String getTownFoundedTime(@NotNull String townName) {
        return findTownByName(townName).map(town -> town.getFoundedTime().format(DateTimeFormatter
                        .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                        .withLocale(Locale.getDefault())
                        .withZone(ZoneId.systemDefault())))
                .orElse(null);
    }

    // Internal - find a town by name
    private Optional<Town> findTownByName(@NotNull String townName) {
        return plugin.getTowns().stream()
                .filter(town -> town.getName().equalsIgnoreCase(townName))
                .findFirst();
    }

}
