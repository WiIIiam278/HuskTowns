package net.william278.husktowns.town;

import com.google.gson.annotations.Expose;
import de.themoep.minedown.adventure.MineDown;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Log;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.config.Locales;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private RuleSet rules;

    @Expose
    private long claims;

    @Expose
    private long level;

    @Expose
    private BigDecimal money;

    @Nullable
    @Expose
    private Spawn spawn;

    @Expose
    private Log log;

    private Town(int id, @NotNull String name, @Nullable String bio, @Nullable String greeting,
                 @Nullable String farewell, @NotNull Map<UUID, Integer> members, @NotNull RuleSet rules,
                 long claims, @NotNull BigDecimal money, long level, @Nullable Spawn spawn,
                 @NotNull Log log, @NotNull Color color) {
        this.id = id;
        this.name = name;
        this.bio = bio;
        this.greeting = greeting;
        this.farewell = farewell;
        this.members = members;
        this.rules = rules;
        this.claims = claims;
        this.money = money;
        this.level = level;
        this.spawn = spawn;
        this.log = log;
        this.color = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    @SuppressWarnings("unused")
    private Town() {
    }

    public static Town of(int id, @NotNull String name, @Nullable String bio, @Nullable String greeting,
                          @Nullable String farewell, @NotNull Map<UUID, Integer> members, @NotNull RuleSet rules,
                          long claims, @NotNull BigDecimal money, long level, @Nullable Spawn spawn,
                          @NotNull Log log, @NotNull Color color) {
        return new Town(id, name, bio, greeting, farewell, members, rules, claims, money, level, spawn, log, color);
    }

    @NotNull
    public static Color getRandomColor(String nameSeed) {
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
    public RuleSet getRules() {
        return rules;
    }

    public void setRules(@NotNull RuleSet ruleSet) {
        this.rules = ruleSet;
    }

    public long getClaimCount() {
        return claims;
    }

    public long getMaxClaims() {
        return level * 5L; //todo Pull level limits
    }

    public long getMaxMembers() {
        return level * 5L; //todo Pull level limits
    }

    public void setClaimCount(long claims) {
        this.claims = claims;
    }

    @NotNull
    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(@NotNull BigDecimal money) {
        this.money = money;
    }

    public long getLevel() {
        return level;
    }

    public void setLevel(long level) {
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        final Town town = (Town) obj;
        return id == town.id;
    }
}
