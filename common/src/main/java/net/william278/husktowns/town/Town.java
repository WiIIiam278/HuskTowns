/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.husktowns.town;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.*;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Log;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Rules;
import net.william278.husktowns.config.Roles;
import net.william278.husktowns.user.User;
import net.william278.husktowns.war.War;
import org.jetbrains.annotations.ApiStatus;
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
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Town {

    // Represents the schema version of the town object
    public static final int CURRENT_SCHEMA = 1;

    // Town ID is stored as the primary key in the database towns table
    @Builder.Default
    private int id = 0;
    @Expose
    private String name;
    @Expose
    @Builder.Default
    private Options options = Options.empty();
    @Expose
    @Builder.Default
    private Map<UUID, Integer> members = Maps.newHashMap();
    @Expose
    private Map<Claim.Type, Rules> rules;
    @Expose
    @Builder.Default
    private int claims = 0;
    @Expose
    @Builder.Default
    private int level = 1;
    @Expose
    @Builder.Default
    private BigDecimal money = BigDecimal.ZERO;
    @Nullable
    @Expose
    @Builder.Default
    private Spawn spawn = null;
    @Expose
    @Builder.Default
    private Log log = Log.empty();
    @Expose
    @Builder.Default
    private Map<Bonus, Integer> bonuses = Maps.newHashMap();
    @Expose
    @Nullable
    @SerializedName("current_war")
    @Builder.Default
    private War currentWar = null;
    @Expose
    @Builder.Default
    private Map<Integer, Relation> relations = Maps.newHashMap();
    @Expose
    @Builder.Default
    private Map<String, String> metadata = Maps.newHashMap();
    @Expose
    @SerializedName("schema_version")
    private int schemaVersion;

    /**
     * Create an admin town
     *
     * @param plugin the HuskTowns plugin instance
     * @return The administrator town
     */
    @NotNull
    @ApiStatus.Internal
    public static Town admin(@NotNull HuskTowns plugin) {
        return Town.builder()
            .name(plugin.getSettings().getTowns().getAdminTown().getName())
            .options(Options.admin(plugin))
            .rules(Map.of(Claim.Type.CLAIM, plugin.getRulePresets().getAdminClaimRules(plugin.getFlags())))
            .metadata(Map.of(plugin.getKey("admin_town").toString(), "true"))
            .schemaVersion(CURRENT_SCHEMA)
            .build();
    }

    /**
     * Create a new town with a mayor and name
     *
     * @param name   The name of the town
     * @param mayor  The mayor of the town
     * @param plugin The HuskTowns plugin instance
     * @return The new town
     */
    @NotNull
    @ApiStatus.Internal
    public static Town create(@NotNull String name, @NotNull User mayor, @NotNull HuskTowns plugin) {
        return Town.builder()
            .name(name)
            .options(Options.create(name))
            .rules(plugin.getRulePresets().getDefaultRules().getDefaults(plugin.getFlags()))
            .log(Log.newTownLog(mayor))
            .members(Maps.newHashMap(Map.of(mayor.getUuid(), plugin.getRoles().getMayorRole().getWeight())))
            .schemaVersion(CURRENT_SCHEMA)
            .build();
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
        return this.options.getBio();
    }

    /**
     * Set the town bio
     *
     * @param bio the new town bio
     * @apiNote This should be passed through {@link net.william278.husktowns.util.Validator#isValidTownMetadata(String)} first
     */
    public void setBio(@NotNull String bio) {
        this.options.setBio(bio);
    }

    /**
     * Get the town greeting message, displayed when a player enters a town claim, if there is one.
     * <p>
     * A default message is used if none is set.
     *
     * @return the town bio wrapped in an {@link Optional}; empty if there is no bio
     */
    public Optional<String> getGreeting() {
        return this.options.getGreeting();
    }

    /**
     * Set the town greeting message, displayed when a player enters a town claim
     *
     * @param greeting the new town greeting message
     * @apiNote This should be passed through {@link net.william278.husktowns.util.Validator#isValidTownMetadata(String)} first
     */
    public void setGreeting(@NotNull String greeting) {
        this.options.setGreeting(greeting);
    }

    /**
     * Get the town farewell message, displayed when a player leaves a town claim into wilderness, if there is one.
     * <p>
     * A default message is used if none is set.
     *
     * @return the town bio wrapped in an {@link Optional}; empty if there is no bio
     */
    public Optional<String> getFarewell() {
        return this.options.getFarewell();
    }

    /**
     * Set the town farewell message, displayed when a player leaves a town claim into wilderness
     *
     * @param farewell the new town farewell message
     * @apiNote This should be passed through {@link net.william278.husktowns.util.Validator#isValidTownMetadata(String)} first
     */
    public void setFarewell(@NotNull String farewell) {
        this.options.setFarewell(farewell);
    }

    /**
     * Get the setColor of the town as a {@link Color}
     *
     * @return the {@link Color} of the town
     * @deprecated use {@link #getDisplayColor()} to get an adventure {@link TextColor} instead
     */
    @NotNull
    @Deprecated(since = "2.5.3")
    public Color getColor() {
        return Color.decode(options.getColor());
    }

    @NotNull
    public TextColor getDisplayColor() {
        return Objects.requireNonNull(
            TextColor.fromHexString(options.getColor()),
            String.format("Invalid setColor hex string (\"%s\") for town %s", options.getColor(), getName())
        );
    }

    /**
     * Get the {@link TextColor} of the town as a hex string, including the leading {@code #}
     *
     * @return the {@link TextColor} of the town as a hex string (e.g. {@code #FF0000})
     */
    @NotNull
    public String getColorRgb() {
        return options.getColor();
    }

    /**
     * Set the {@link TextColor} of the town
     *
     * @param color the new {@link TextColor} of the town
     */
    public void setTextColor(@NotNull TextColor color) {
        this.options.setColor(color.asHexString());
    }

    /**
     * Set the {@link Color} of the town
     *
     * @param color the new {@link Color} of the town
     * @deprecated use {@link #setTextColor(TextColor)} to set an adventure {@link TextColor} instead
     */
    @Deprecated(since = "2.5.3")
    public void setColor(@NotNull Color color) {
        this.options.setColor(String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()));
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
            .orElseThrow(() -> new IllegalStateException("Town \"" + getName() + "\" has no mayor"));
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
            throw new IllegalArgumentException("Cannot remove the mayor of the town \"" + getName() + "\"");
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
     * Get the town's ID
     *
     * @return the town's ID
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
        return (plugin.getLevels().getCropGrowthRateBonus(getLevel()) + getBonus(Bonus.CROP_GROWTH_RATE)) / 100d;
    }

    /**
     * Get the bonus mob spawner spawn rate in farm chunks for this town
     * <p>
     * This will return a value between 0 and 1, where 0 is no bonus and 1 is a 100% bonus.
     *
     * @return the bonus mob spawner spawn rate in farm chunks for this town
     */
    public double getMobSpawnerRate(@NotNull HuskTowns plugin) {
        return (plugin.getLevels().getMobSpawnerRateBonus(getLevel()) + getBonus(Bonus.MOB_SPAWNER_RATE)) / 100d;
    }

    /**
     * Get the current war this town is in, if any
     *
     * @return the current war this town is in, wrapped in an {@link Optional}; empty if the town is not in a war
     * @since 2.6
     */
    public Optional<War> getCurrentWar() {
        return Optional.ofNullable(currentWar);
    }

    /**
     * Returns whether this town is at war with another town
     *
     * @param otherTown the town to check
     * @return {@code true} if this town is at war with the other town
     * @since 2.6
     */
    public boolean isAtWarWith(@NotNull Town otherTown) {
        final Optional<War> currentWar = getCurrentWar();
        if (currentWar.isPresent()) {
            final War war = currentWar.get();
            return war.getDefending() == getId() && war.getAttacking() == otherTown.getId()
                || war.getAttacking() == getId() && war.getDefending() == otherTown.getId();
        }
        return false;
    }

    /**
     * Set the current war this town is in
     *
     * @param currentWar the current war this town is in
     * @since 2.6
     */
    public void setCurrentWar(@NotNull War currentWar) {
        this.currentWar = currentWar;
    }

    /**
     * Clear the current war this town is in
     *
     * @since 2.6
     */
    public void clearCurrentWar() {
        this.currentWar = null;
    }


    /**
     * Get the relations this town has, as a map of {@link Relation}s to town IDs
     *
     * @return the relations this town has
     * @since 2.6
     */
    @NotNull
    public Map<Integer, Relation> getRelations() {
        return relations == null ? relations = new HashMap<>() : relations;
    }

    /**
     * Get the relations this town has, as a map of {@link Relation}s to {@link Town}s
     *
     * @param plugin the HuskTowns plugin instance
     * @return the relations this town has
     * @since 2.6
     */
    @NotNull
    public Map<Town, Relation> getRelations(@NotNull HuskTowns plugin) {
        return getRelations().entrySet().stream()
            .filter(e -> plugin.findTown(e.getKey()).isPresent())
            .collect(Collectors.toMap(
                e -> plugin.findTown(e.getKey()).orElse(null),
                Map.Entry::getValue
            ));
    }

    /**
     * Get what this town thinks of another town
     *
     * @param otherTown the town to get the relation with
     * @return the relation this town has with the given town
     * @since 2.6
     */
    @NotNull
    public Relation getRelationWith(@NotNull Town otherTown) {
        return relations.getOrDefault(otherTown.getId(), Relation.NEUTRAL);
    }

    /**
     * Set what this town thinks of another town
     *
     * @param otherTown the town to set the relation with
     * @param relation  the relation to set
     * @since 2.6
     */
    public void setRelationWith(@NotNull Town otherTown, @NotNull Relation relation) {
        if (relation == Relation.NEUTRAL) {
            relations.remove(otherTown.getId());
            return;
        }
        relations.put(otherTown.getId(), relation);
    }


    /**
     * Returns whether this town's feelings about another town are mutual (the same)
     *
     * @param otherTown the town to check the relation with
     * @param relation  the relation to check
     * @return {@code true} if this town's feelings about another town are equal (e.g. both allied, neutral, or enemy)
     * @since 2.6
     */
    public boolean areRelationsBilateral(@NotNull Town otherTown, @NotNull Relation relation) {
        if (otherTown.equals(this)) {
            return true;
        }
        return getRelationWith(otherTown) == relation && otherTown.getRelationWith(this) == relation;
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
            throw new IllegalStateException("Invalid key in town \"" + getName() + "\" metadata", e);
        }
    }

    /**
     * Get the schema version of this town object
     *
     * @return the schema version of this town object
     */
    @ApiStatus.Internal
    public int getSchemaVersion() {
        return schemaVersion;
    }

    /**
     * Set the schema version of this town object
     *
     * @param schemaVersion the schema version of this town object
     */
    @ApiStatus.Internal
    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    /**
     * Carries out town schema object upgrades
     *
     * @param json the JSON-ified town
     * @param gson the Gson instance
     * @return the upgraded town
     */
    @ApiStatus.Internal
    @NotNull
    public Town upgradeSchema(String json, Gson gson) {
        if (this.schemaVersion >= CURRENT_SCHEMA) {
            return this;
        }

        // Perform schema upgrades
        if (this.schemaVersion == 0) {
            final Map<?, ?> map = gson.fromJson(json, Map.class);
            final Options.OptionsBuilder builder = Options.builder();
            if (map.containsKey("bio")) {
                builder.bio((String) map.get("bio"));
            }
            if (map.containsKey("greeting")) {
                builder.greeting((String) map.get("greeting"));
            }
            if (map.containsKey("farewell")) {
                builder.farewell((String) map.get("farewell"));
            }
            if (map.containsKey("color")) {
                builder.color((String) map.get("color"));
            }
            this.relations = Maps.newHashMap();
            this.options = builder.build();
            setSchemaVersion(1);
        }
        return this;
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
     * Represents town options
     */
    @Builder
    @Setter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Options {
        @Expose
        @Nullable
        @Builder.Default
        private String bio = null;
        @Expose
        @Nullable
        @Builder.Default
        private String greeting = null;
        @Expose
        @Nullable
        @Builder.Default
        private String farewell = null;
        @Getter
        @Expose
        @Builder.Default
        private String color = NamedTextColor.GRAY.asHexString();

        @NotNull
        public static Options admin(@NotNull HuskTowns plugin) {
            return Options.builder()
                .color(plugin.getSettings().getTowns().getAdminTown().getColor().asHexString())
                .greeting(plugin.getLocales().getRawLocale("entering_admin_claim").orElse(null))
                .farewell(plugin.getLocales().getRawLocale("leaving_admin_claim").orElse(null))
                .build();
        }

        @NotNull
        public static Options empty() {
            return Options.builder().build();
        }

        @NotNull
        public static Options create(@NotNull String townName) {
            return Options.builder()
                .color(getRandomTextColor(townName).asHexString())
                .build();
        }

        public Optional<String> getBio() {
            return Optional.ofNullable(bio);
        }

        public Optional<String> getGreeting() {
            return Optional.ofNullable(greeting);
        }

        public Optional<String> getFarewell() {
            return Optional.ofNullable(farewell);
        }

        /**
         * Generate a random town color seeded from the town's name
         *
         * @param nameSeed The town's name
         * @return A random color
         */
        @NotNull
        public static TextColor getRandomTextColor(@NotNull String nameSeed) {
            final Random random = new Random(nameSeed.hashCode());
            return TextColor.color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }
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

    /**
     * Represents relations a town can have with other towns
     *
     * @since 2.6
     */
    public enum Relation {
        /**
         * This town is an ally. If friendly fire is disabled, PvP on allied towns will be disabled.
         */
        ALLY,
        /**
         * This town is neutral.
         * </p>
         * All towns are neutral by default, so this is only used when adding/removing relations.
         */
        NEUTRAL,
        /**
         * This town is an enemy. If the war system is enabled, mutual enemy towns can go to war.
         */
        ENEMY;

        /**
         * Parse a {@link Relation} from a string name
         *
         * @param string the string to parse
         * @return the parsed {@link Relation} wrapped in an {@link Optional}, if any was found
         */
        public static Optional<Relation> parse(@NotNull String string) {
            return Arrays.stream(values())
                .filter(operation -> operation.name().equalsIgnoreCase(string))
                .findFirst();
        }
    }
}
