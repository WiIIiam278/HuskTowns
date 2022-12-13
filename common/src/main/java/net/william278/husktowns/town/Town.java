package net.william278.husktowns.town;

import net.william278.husktowns.audit.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class Town {

    private UUID uuid;

    private String name;

    private String bio;

    private Map<UUID, Integer> members;

    private RuleSet rules;

    private long level;

    private BigDecimal money;

    @Nullable
    private Spawn spawn;

    private Log log;

    private Town(@NotNull UUID uuid, @NotNull String name, @NotNull String bio, @NotNull Map<UUID, Integer> members,
                 @NotNull RuleSet rules, @NotNull BigDecimal money, long level, @Nullable Spawn spawn, @NotNull Log log) {
        this.uuid = uuid;
        this.name = name;
        this.bio = bio;
        this.members = members;
        this.rules = rules;
        this.money = money;
        this.level = level;
        this.spawn = spawn;
        this.log = log;
    }

    public static Town of(@NotNull UUID uuid, @NotNull String name, @NotNull String bio, @NotNull Map<UUID, Integer> members,
                          @NotNull RuleSet rules, @NotNull BigDecimal money, long level, @NotNull Spawn spawn, @NotNull Log log) {
        return new Town(uuid, name, bio, members, rules, money, level, spawn, log);
    }

    @SuppressWarnings("unused")
    private Town() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Map<UUID, Integer> getMembers() {
        return members;
    }

    public void addMember(UUID uuid, int role) {
        this.members.put(uuid, role);
    }

    public void removeMember(UUID uuid) {
        this.members.remove(uuid);
    }

    public RuleSet getRules() {
        return rules;
    }

    public void setRules(RuleSet ruleSet) {
        this.rules = ruleSet;
    }

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
