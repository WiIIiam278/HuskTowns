package net.william278.husktowns.town;

import com.google.gson.annotations.Expose;
import net.william278.husktowns.audit.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Town {

    @Expose
    private int id;

    @Expose
    private String name;

    @Nullable
    @Expose
    private String bio;

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

    @Expose
    private Color color;

    private Town(int id, @NotNull String name, @Nullable String bio, @NotNull Map<UUID, Integer> members,
                 @NotNull RuleSet rules, long claims, @NotNull BigDecimal money, long level, @Nullable Spawn spawn,
                 @NotNull Log log, @NotNull Color color) {
        this.id = id;
        this.name = name;
        this.bio = bio;
        this.members = members;
        this.rules = rules;
        this.claims = claims;
        this.money = money;
        this.level = level;
        this.spawn = spawn;
        this.log = log;
        this.color = color;
    }

    @SuppressWarnings("unused")
    private Town() {
    }

    public static Town of(int id, @NotNull String name, @Nullable String bio, @NotNull Map<UUID, Integer> members,
                          @NotNull RuleSet rules, long claims, @NotNull BigDecimal money, long level, @Nullable Spawn spawn,
                          @NotNull Log log, @NotNull Color color) {
        return new Town(id, name, bio, members, rules, claims, money, level, spawn, log, color);
    }

    @NotNull
    public static Color getRandomColor(String nameSeed) {
        Random random = new Random(nameSeed.hashCode());
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

    @Nullable
    public String getBio() {
        return bio;
    }

    public void setBio(@NotNull String bio) {
        this.bio = bio;
    }

    @NotNull
    public Map<UUID, Integer> getMembers() {
        return members;
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

    public long getClaims() {
        return claims;
    }

    public void setClaims(long claims) {
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

    @Nullable
    public Spawn getSpawn() {
        return spawn;
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
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

}
