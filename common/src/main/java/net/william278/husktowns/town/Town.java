package net.william278.husktowns.town;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Log;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class Town {

    private UUID uuid;

    private String name;

    @Nullable
    private String bio;

    private Map<UUID, Integer> members;

    private RuleSet rules;

    private long claims;

    private long level;

    private BigDecimal money;

    @Nullable
    private Spawn spawn;

    private Log log;

    private Town(@NotNull UUID uuid, @NotNull String name, @Nullable String bio, @NotNull Map<UUID, Integer> members,
                 @NotNull RuleSet rules, long claims, @NotNull BigDecimal money, long level, @Nullable Spawn spawn,
                 @NotNull Log log) {
        this.uuid = uuid;
        this.name = name;
        this.bio = bio;
        this.members = members;
        this.rules = rules;
        this.claims = claims;
        this.money = money;
        this.level = level;
        this.spawn = spawn;
        this.log = log;
    }

    public static Town of(@NotNull UUID uuid, @NotNull String name, @NotNull String bio, @NotNull Map<UUID, Integer> members,
                          @NotNull RuleSet rules, long claims, @NotNull BigDecimal money, long level, @NotNull Spawn spawn,
                          @NotNull Log log) {
        return new Town(uuid, name, bio, members, rules, claims, money, level, spawn, log);
    }

    //todo pull default ruleset
    public static Town create(@NotNull String name, @NotNull User creator, @NotNull HuskTowns plugin) {
        return new Town(UUID.randomUUID(), name, null,
                Map.of(creator.getUuid(), plugin.getRoles().getMayor().getWeight()),
                RuleSet.of(Map.of()), 0, BigDecimal.ZERO, 0, null, Log.newTownLog(creator));
    }

    @SuppressWarnings("unused")
    private Town() {
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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

    public void addMember(UUID uuid, int role) {
        this.members.put(uuid, role);
    }

    public void removeMember(UUID uuid) {
        this.members.remove(uuid);
    }

    @NotNull
    public RuleSet getRules() {
        return rules;
    }

    public void setRules(RuleSet ruleSet) {
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

    public void setMoney(BigDecimal money) {
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

    public void setLog(Log log) {
        this.log = log;
    }
}
