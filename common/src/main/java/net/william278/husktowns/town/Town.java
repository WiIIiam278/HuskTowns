package net.william278.husktowns.town;

import com.google.gson.annotations.Expose;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Log;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Rules;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a town object
 */
@SuppressWarnings("unused")
public class Town {
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
                0, BigDecimal.ZERO, 0, null, Log.empty(), Color.RED, Map.of(),
                Map.of("admin_town", "true"));
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

    public int getId() {
        return id;
    }

    public void updateId(int id) {
        this.id = id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Optional<String> getBio() {
        return Optional.ofNullable(bio);
    }

    public void setBio(@NotNull String bio) {
        this.bio = bio;
    }

    public Optional<String> getGreeting() {
        return Optional.ofNullable(greeting);
    }

    public void setGreeting(@NotNull String greeting) {
        this.greeting = greeting;
    }

    public Optional<String> getFarewell() {
        return Optional.ofNullable(farewell);
    }

    public void setFarewell(@NotNull String farewell) {
        this.farewell = farewell;
    }

    @NotNull
    public Map<UUID, Integer> getMembers() {
        return members;
    }

    @NotNull
    public UUID getMayor() {
        return members.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException("Town has no mayor"));
    }

    public void addMember(@NotNull UUID uuid, @NotNull Role role) {
        this.members.put(uuid, role.getWeight());
    }

    public void removeMember(@NotNull UUID uuid) {
        this.members.remove(uuid);
    }

    @NotNull
    public Map<Claim.Type, Rules> getRules() {
        return rules;
    }

    public int getClaimCount() {
        return claims;
    }

    public int getMaxClaims(@NotNull HuskTowns plugin) {
        return plugin.getLevels().getMaxClaims(level) + getBonus(Bonus.CLAIMS);
    }

    public int getMaxMembers(@NotNull HuskTowns plugin) {
        return plugin.getLevels().getMaxMembers(level) + getBonus(Bonus.MEMBERS);
    }

    public void setClaimCount(int claims) {
        this.claims = claims;
    }

    @NotNull
    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(@NotNull BigDecimal money) {
        this.money = money.max(BigDecimal.ZERO);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Optional<Spawn> getSpawn() {
        return Optional.ofNullable(spawn);
    }

    public void setSpawn(@NotNull Spawn spawn) {
        this.spawn = spawn;
    }

    public void clearSpawn() {
        this.spawn = null;
    }

    @NotNull
    public Log getLog() {
        return log;
    }

    @NotNull
    public OffsetDateTime getFoundedTime() {
        return log.getFoundedTime();
    }

    @NotNull
    public Color getColor() {
        return Color.decode(color);
    }

    @NotNull
    public String getColorRgb() {
        return color;
    }

    public void setColor(@NotNull Color color) {
        this.color = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public int getBonus(@NotNull Bonus bonus) {
        return bonuses.getOrDefault(bonus, 0);
    }

    public void setBonus(@NotNull Bonus bonus, int amount) {
        this.bonuses.put(bonus, amount);
    }

    public int getBonusClaims() {
        return getBonus(Bonus.CLAIMS);
    }

    public void setBonusClaims(int bonusClaims) {
        setBonus(Bonus.CLAIMS, bonusClaims);
    }

    public int getBonusMembers() {
        return getBonus(Bonus.MEMBERS);
    }

    public void setBonusMembers(int bonusMembers) {
        setBonus(Bonus.MEMBERS, bonusMembers);
    }

    public double getCropGrowthRate(@NotNull HuskTowns plugin) {
        return plugin.getLevels().getCropGrowthRateBonus(getLevel())  + Math.min(getBonus(Bonus.CROP_GROWTH_RATE), 100d) / 100d;
    }

    public double getMobSpawnerRate(@NotNull HuskTowns plugin) {
        return plugin.getLevels().getMobSpawnerRateBonus(getLevel()) + Math.min(getBonus(Bonus.MOB_SPAWNER_RATE), 100d) / 100d;
    }

    public void setMetadataTag(@NotNull Key key, @NotNull String value) {
        this.metadata.put(key.toString(), value);
    }

    public Optional<String> getMetadataTag(@NotNull Key key) {
        return Optional.ofNullable(metadata.get(key.toString()));
    }

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
        CLAIMS,
        MEMBERS,
        CROP_GROWTH_RATE,
        MOB_SPAWNER_RATE;

        public static Optional<Bonus> parse(@NotNull String string) {
            return Arrays.stream(values())
                    .filter(operation -> operation.name().equalsIgnoreCase(string))
                    .findFirst();
        }
    }
}
