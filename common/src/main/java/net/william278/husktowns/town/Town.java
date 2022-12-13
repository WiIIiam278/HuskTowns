package net.william278.husktowns.town;

import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Rules;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class Town {

    private UUID uuid;

    private String name;

    private String bio;

    private Map<UUID, Integer> members;

    private Map<Claim.Type, Rules> rules;

    private BigDecimal money;

    private long level;

    private Town(@NotNull UUID uuid, @NotNull String name, @NotNull String bio, @NotNull Map<UUID, Integer> members,
                @NotNull Map<Claim.Type, Rules> rules, @NotNull BigDecimal money, long level) {
        this.uuid = uuid;
        this.name = name;
        this.bio = bio;
        this.members = members;
        this.rules = rules;
        this.money = money;
        this.level = level;
    }

    public static Town of(@NotNull UUID uuid, @NotNull String name, @NotNull String bio, @NotNull Map<UUID, Integer> members,
                          @NotNull Map<Claim.Type, Rules> rules, @NotNull BigDecimal money, long level) {
        return new Town(uuid, name, bio, members, rules, money, level);
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
        this.members = members;
    }

    public Map<Claim.Type, Rules> getRules() {
        return rules;
    }

    public void setRules(Map<Claim.Type, Rules> rules) {
        this.rules = rules;
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
}
