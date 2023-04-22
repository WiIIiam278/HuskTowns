/*
 * This file is part of HuskTowns by William278. Do not redistribute!
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  All rights reserved.
 *
 *  This source code is provided as reference to licensed individuals that have purchased the HuskTowns
 *  plugin once from any of the official sources it is provided. The availability of this code does
 *  not grant you the rights to modify, re-distribute, compile or redistribute this source code or
 *  "plugin" outside this intended purpose. This license does not cover libraries developed by third
 *  parties that are utilised in the plugin.
 */

package net.william278.husktowns.town;

import com.google.gson.annotations.Expose;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Log;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Rules;
import net.william278.husktowns.config.Roles;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a Town, of which players can be a member of and can have claims. Various other properties govern
 * how players can interact with the town.
 * <p>
 * To create a town (<b>internal only</b>), see {@link Town#create(String, User, HuskTowns)}
 */
@SuppressWarnings("unused")
public class Town {

    // Town ID is stored as the primary key in the database towns table
    private int id;
    @Expose
    private String name;
    @Nullable
    @Expose
    private String bio;
    @Nullable
    @Expose
    private String greeting;
    @Nullable
    @Expose
    private String farewell;
    @Expose
    private String color;
    @Expose
    private Map<UUID, Integer> members;
    @Expose
    private Map<Claim.Type, Rules> rules;
    @Expose
    private int claims;
    @Expose
    private int level;
    @Expose
    private BigDecimal money;
    @Nullable
    @Expose
    private Spawn spawn;
    @Expose
    private Log log;
    @Expose
    private Map<Bonus, Integer> bonuses;
    @Expose
    private Map<String, String> metadata;

    // Internal fat constructor for instantiating a town
    private Town(int id, @NotNull String name, @Nullable String bio, @Nullable String greeting,
                 @Nullable String farewell, @NotNull Map<UUID, Integer> members, @NotNull Map<Claim.Type, Rules> rules,
                 int claims, @NotNull BigDecimal money, int level, @Nullable Spawn spawn, @NotNull Log log,
                 @NotNull Color color, @NotNull Map<Bonus, Integer> bonuses, @NotNull Map<String, String> metadata) {
        this.id = id;
        this.name = name;
        this.bio = bio;
        this.greeting = greeting;
        this.farewell = farewell;
        this.members = members;
        this.rules = rules;
        this.claims = claims;
        this.money = money.max(BigDecimal.ZERO);
        this.level = level;
        this.spawn = spawn;
        this.log = log;
        this.color = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        this.bonuses = bonuses;
        this.metadata = metadata;
    }

    @SuppressWarnings("unused")
    private Town() {
    }

    /**
     * Create a town from pre-existing data properties
     *
     * @param id       the town ID
     * @param name     the town name
     * @param bio      the town bio, or {@code null} if none
     * @param greeting the town greeting message, or {@code null} if none
     * @param farewell the town farewell message, or {@code null} if none
     * @param members  Map of town member {@link UUID}s to role weights
     * @param rules    Map of {@link Claim.Type}s to {@link Rules flag rule mappings}
     * @param claims   the number of claims the town has made
     * @param money    the town's balance
     * @param level    the town's level always ({@code >= 1})
     * @param spawn    the town's spawn, or {@code null} if none
     * @param log      the town's audit {@link Log}
     * @param color    the town's color
     * @param bonuses  the town's {@link Bonus} levels
     * @param metadata the town's metadata tag map
     * @return a new {@link Town} instance
     */
    @NotNull
    public static Town of(int id, @NotNull String name, @Nullable String bio, @Nullable String greeting,
                          @Nullable String farewell, @NotNull Map<UUID, Integer> members,
                          @NotNull Map<Claim.Type, Rules> rules, int claims, @NotNull BigDecimal money, int level,
                          @Nullable Spawn spawn, @NotNull Log log, @NotNull Color color,
                          @NotNull Map<Bonus, Integer> bonuses, @NotNull Map<String, String> metadata) {
        return new Town(id, name, bio, greeting, farewell, members, rules, claims, money, level, spawn, log, color,
                bonuses, metadata);
    }

    /**
     * Create a new user town
     *
     * @param name    The name of the town
     * @param creator The creator of the town
     * @param plugin  The plugin
     * @return A town, with {@code id} set to 0 of the name {@code name}, with the creator as the only member and mayor
     */
    @NotNull
    public static Town create(@NotNull String name, @NotNull User creator, @NotNull HuskTowns plugin) {
        return of(0, name, null, null, null, new HashMap<>(),
                plugin.getRulePresets().getDefaultClaimRules(), 0, BigDecimal.ZERO, 1, null,
                Log.newTownLog(creator), Town.getRandomColor(name), new HashMap<>(), new HashMap<>());
    }

    /**
     * Get the admin town
     *
     * @param plugin the HuskTowns plugin instance
     * @return The administrator town
     */
    @NotNull
    public static Town admin(@NotNull HuskTowns plugin) {
        return new Town(0, plugin.getSettings().getAdminTownName(), null,
                plugin.getLocales().getRawLocale("entering_admin_claim").orElse(null),
                plugin.getLocales().getRawLocale("leaving_admin_claim").orElse(null),
                Map.of(), Map.of(Claim.Type.CLAIM, plugin.getRulePresets().getAdminClaimRules()),
                0, BigDecimal.ZERO, 0, null, Log.empty(), plugin.getSettings().getAdminTownColor(),
                Map.of(), Map.of(plugin.getKey("admin_town").toString(), "true"));
    }

    /**
     * Generate a random town color seeded from the town's name
     *
     * @param nameSeed The town's name
     * @return A random color
     */
    @NotNull
    public static Color getRandomColor(@NotNull String nameSeed) {
        final Random random = new Random(nameSeed.hashCode());
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    /**
     * Get the town ID
     *
     * @return the town ID
     */
    public int getId() {
        return id;
    }

    /**
     * Set the town ID
     *
     * @param id the town ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get the name of the town
     *
     * @return the name of the town
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Set the name of the town
     *
     * @param name the name of the town
     * @apiNote This should be passed through {@link net.william278.husktowns.util.Validator#isValidTownName(String)} first
     */
    public void setName(@NotNull String name) {
        this.name = name;
    }

    /**
     * Get the town bio, if there is one
     *
     * @return the town bio wrapped in an {@link Optional}; empty if there is no bio
     */
    public Optional<String> getBio() {
        return Optional.ofNullable(bio);
    }

    /**
     * Set the town bio
     *
     * @param bio the new town bio
     * @apiNote This should be passed through {@link net.william278.husktowns.util.Validator#isValidTownMetadata(String)} first
     */
    public void setBio(@NotNull String bio) {
        this.bio = bio;
    }

    /**
     * Get the town greeting message, displayed when a player enters a town claim, if there is one.
     * <p>
     * A default message is used if none is set.
     *
     * @return the town bio wrapped in an {@link Optional}; empty if there is no bio
     */
    public Optional<String> getGreeting() {
        return Optional.ofNullable(greeting);
    }

    /**
     * Set the town greeting message, displayed when a player enters a town claim
     *
     * @param greeting the new town greeting message
     * @apiNote This should be passed through {@link net.william278.husktowns.util.Validator#isValidTownMetadata(String)} first
     */
    public void setGreeting(@NotNull String greeting) {
        this.greeting = greeting;
    }

    /**
     * Get the town farewell message, displayed when a player leaves a town claim into wilderness, if there is one.
     * <p>
     * A default message is used if none is set.
     *
     * @return the town bio wrapped in an {@link Optional}; empty if there is no bio
     */
    public Optional<String> getFarewell() {
        return Optional.ofNullable(farewell);
    }

    /**
     * Set the town farewell message, displayed when a player leaves a town claim into wilderness
     *
     * @param farewell the new town farewell message
     * @apiNote This should be passed through {@link net.william278.husktowns.util.Validator#isValidTownMetadata(String)} first
     */
    public void setFarewell(@NotNull String farewell) {
        this.farewell = farewell;
    }

    /**
     * Get the map of town member {@link UUID}s to their {@link Role#getWeight() Role weights}
     *
     * @return The map of members of this town to their role weights
     */
    @NotNull
    public Map<UUID, Integer> getMembers() {
        return members;
    }

    /**
     * Get the {@link UUID} of the town's mayor
     *
     * @return The UUID of the town's mayor
     * @implNote This is determined by the user who has the highest role weight in {@link #getMembers() the members map},
     * though should only ever be the individual with the {@link Roles#getMayorRole() Mayor role}.
     */
    @NotNull
    public UUID getMayor() {
        return members.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException("Town has no mayor"));
    }

    /**
     * Add a new member to the town
     *
     * @param uuid The {@link UUID} of the member to add
     * @param role The {@link Role} to add the member as
     */
    public void addMember(@NotNull UUID uuid, @NotNull Role role) {
        this.members.put(uuid, role.getWeight());
    }

    /**
     * Remove a member from the town
     *
     * @param uuid The {@link UUID} of the member to remove
     * @throws IllegalArgumentException if you attempt to remove the town's mayor
     */
    public void removeMember(@NotNull UUID uuid) throws IllegalArgumentException {
        if (getMayor().equals(uuid)) {
            throw new IllegalArgumentException("Cannot remove the mayor of the town");
        }
        this.members.remove(uuid);
    }

    /**
     * Get the map of {@link Claim.Type} to the {@link Rules flag rule mappings} for this town
     *
     * @return the map of {@link Claim.Type} to the {@link Rules} for this town
     */
    @NotNull
    public Map<Claim.Type, Rules> getRules() {
        return rules;
    }

    /**
     * Get the number of claims this town has created
     *
     * @return the number of claims this town has created
     */
    public int getClaimCount() {
        return claims;
    }

    /**
     * Set the number of claims this town has created
     *
     * @param claims the number of claims this town has created
     */
    public void setClaimCount(int claims) {
        this.claims = claims;
    }

    /**
     * Get the maximum number of claims this town can create
     *
     * @param plugin Instance of the HuskTowns plugin
     * @return the maximum number of claims this town can create
     */
    public int getMaxClaims(@NotNull HuskTowns plugin) {
        return plugin.getLevels().getMaxClaims(level) + getBonus(Bonus.CLAIMS);
    }

    /**
     * Get the maximum number of members this town can have in it
     *
     * @param plugin Instance of the HuskTowns plugin
     * @return the maximum number of members this town can have
     */
    public int getMaxMembers(@NotNull HuskTowns plugin) {
        return plugin.getLevels().getMaxMembers(level) + getBonus(Bonus.MEMBERS);
    }

    /**
     * Get the amount of money this town has
     *
     * @return the amount of money this town has as a {@link BigDecimal}
     */
    @NotNull
    public BigDecimal getMoney() {
        return money;
    }

    /**
     * Set the amount of money this town has
     *
     * @param money the amount of money this town has as a {@link BigDecimal}
     * @implNote This value cannot be less than 0. If you attempt to set it to a negative value, it will be set to 0.
     */
    public void setMoney(@NotNull BigDecimal money) {
        this.money = money.max(BigDecimal.ZERO);
    }

    /**
     * Get the town's level
     *
     * @return the town's level. This value is always {@code >= 1}
     */
    public int getLevel() {
        return level;
    }

    /**
     * Set the town's level
     *
     * @param level the town's level. This value must be {@code >= 1}
     * @throws IllegalArgumentException if the level is less than 1
     */
    public void setLevel(int level) throws IllegalArgumentException {
        if (level < 1) {
            throw new IllegalArgumentException("Level cannot be less than 1");
        }
        this.level = level;
    }

    /**
     * Get the town's {@link Spawn} position, if it has one
     *
     * @return the town's {@link Spawn} position, wrapped in an {@link Optional}; empty if a spawn has not been set
     */
    public Optional<Spawn> getSpawn() {
        return Optional.ofNullable(spawn);
    }

    /**
     * Set the town's {@link Spawn} position
     *
     * @param spawn the town's {@link Spawn} position
     */
    public void setSpawn(@NotNull Spawn spawn) {
        this.spawn = spawn;
    }

    /**
     * Clear the town's {@link Spawn} position
     */
    public void clearSpawn() {
        this.spawn = null;
    }

    /**
     * Get the town's {@link Log audit log}
     *
     * @return the town's {@link Log audit log}
     */
    @NotNull
    public Log getLog() {
        return log;
    }

    /**
     * Get the time at which the town was created
     *
     * @return the time at which the town was created, as an {@link OffsetDateTime}
     */
    @NotNull
    public OffsetDateTime getFoundedTime() {
        return log.getFoundedTime();
    }

    /**
     * Get the {@link Color} of the town
     *
     * @return the {@link Color} of the town
     */
    @NotNull
    public Color getColor() {
        return Color.decode(color);
    }

    /**
     * Get the {@link Color} of the town as a hex string, including the leading {@code #}
     *
     * @return the {@link Color} of the town as a hex string (e.g. {@code #FF0000})
     */
    @NotNull
    public String getColorRgb() {
        return color;
    }

    /**
     * Set the {@link Color} of the town
     *
     * @param color the new {@link Color} of the town
     */
    public void setColor(@NotNull Color color) {
        this.color = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Get the value set for a given town {@link Bonus}
     *
     * @param bonus the type of {@link Bonus} to get the value for
     * @return the value set for the given {@link Bonus}
     */
    public int getBonus(@NotNull Bonus bonus) {
        return bonuses.getOrDefault(bonus, 0);
    }

    /**
     * Set the value for a given town {@link Bonus}
     *
     * @param bonus  the type of {@link Bonus} to set the value for
     * @param amount the new value for the given {@link Bonus}
     */
    public void setBonus(@NotNull Bonus bonus, int amount) {
        this.bonuses.put(bonus, amount);
    }

    /**
     * Get the number of bonus claims this town can make
     *
     * @return the number of bonus claims this town can make
     */
    public int getBonusClaims() {
        return getBonus(Bonus.CLAIMS);
    }

    /**
     * Set the number of bonus claims this town can make
     *
     * @param bonusClaims the number of bonus claims this town can make
     */
    public void setBonusClaims(int bonusClaims) {
        setBonus(Bonus.CLAIMS, bonusClaims);
    }

    /**
     * Get the number of bonus members this town can have
     *
     * @return the number of bonus members this town can have
     */
    public int getBonusMembers() {
        return getBonus(Bonus.MEMBERS);
    }

    /**
     * Set the number of bonus members this town can have
     *
     * @param bonusMembers the number of bonus members this town can have
     */
    public void setBonusMembers(int bonusMembers) {
        setBonus(Bonus.MEMBERS, bonusMembers);
    }

    /**
     * Get the bonus crop growth rate in farm chunks for this town
     * <p>
     * This will return a value between 0 and 1, where 0 is no bonus and 1 is a 100% bonus.
     *
     * @return the bonus crop growth rate in farm chunks for this town
     */
    public double getCropGrowthRate(@NotNull HuskTowns plugin) {
        return plugin.getLevels().getCropGrowthRateBonus(getLevel()) + Math.min(getBonus(Bonus.CROP_GROWTH_RATE), 100d) / 100d;
    }

    /**
     * Get the bonus mob spawner spawn rate in farm chunks for this town
     * <p>
     * This will return a value between 0 and 1, where 0 is no bonus and 1 is a 100% bonus.
     *
     * @return the bonus mob spawner spawn rate in farm chunks for this town
     */
    public double getMobSpawnerRate(@NotNull HuskTowns plugin) {
        return plugin.getLevels().getMobSpawnerRateBonus(getLevel()) + Math.min(getBonus(Bonus.MOB_SPAWNER_RATE), 100d) / 100d;
    }

    /**
     * Get the metadata value for a {@link Key}
     *
     * @param key the {@link Key} to get the value for
     * @return the metadata value for the given {@link Key}, wrapped in an {@link Optional}; empty if no value is set
     */
    public Optional<String> getMetadataTag(@NotNull Key key) {
        return Optional.ofNullable(metadata.get(key.toString()));
    }

    /**
     * Set a metadata tag for this town
     *
     * @param key   the {@link Key} of the metadata tag
     * @param value the value of the metadata tag
     */
    public void setMetadataTag(@NotNull Key key, @NotNull String value) {
        this.metadata.put(key.toString(), value);
    }

    /**
     * Get the mapping of metadata keys to values
     *
     * @return the mapping of metadata keys to values
     */
    @NotNull
    @SuppressWarnings("PatternValidation")
    public Map<Key, String> getMetadataTags() {
        try {
            return metadata.entrySet().stream().collect(Collectors
                    .toMap(entry -> Key.key(entry.getKey()), Map.Entry::getValue));
        } catch (InvalidKeyException e) {
            throw new IllegalStateException("Invalid key in town metadata", e);
        }
    }

    /**
     * Compares this town to another object
     *
     * @param obj the object to compare to
     *            <p>
     *            This will return {@code true} if the other object is a {@link Town} with the same {@link #getId()}.
     * @return true if the other object is a {@link Town} with the same {@link #getId()}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        final Town town = (Town) obj;
        return this.id == town.id;
    }

    /**
     * Identifies different categories of town bonuses
     */
    public enum Bonus {
        /**
         * Number of additional claims this town can make, on top of level-based claims
         */
        CLAIMS,
        /**
         * Number of additional members this town can have, on top of level-based members
         */
        MEMBERS,
        /**
         * Bonus crop growth rate in farm chunks, as a percentage
         */
        CROP_GROWTH_RATE,
        /**
         * Bonus mob spawner spawn rate in farm chunks, as a percentage
         */
        MOB_SPAWNER_RATE;

        /**
         * Parse a {@link Bonus} from a string name
         *
         * @param string the string to parse
         * @return the parsed {@link Bonus} wrapped in an {@link Optional}, if any was found
         */
        public static Optional<Bonus> parse(@NotNull String string) {
            return Arrays.stream(values())
                    .filter(operation -> operation.name().equalsIgnoreCase(string))
                    .findFirst();
        }
    }
}
