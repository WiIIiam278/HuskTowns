package net.william278.husktowns.config;

import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃    HuskTowns Levels Config   ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ This file is for configuring town level requirements and rewards
        ┗╸ Documentation: https://william278.net/docs/husktowns/claim-rules""")
public class Levels {

    @YamlKey("level_money_requirements")
    private Map<String, Double> levelMoneyRequirements = new TreeMap<>();

    @YamlKey("level_member_limits")
    private Map<String, Long> levelMemberLimits = new TreeMap<>();

    @YamlKey("level_claim_limits")
    private Map<String, Long> levelClaimLimits = new TreeMap<>();

    public Levels() {
        for (long i = 0; i < 20; i++) {
            levelMoneyRequirements.put(Long.toString(i), Math.pow(2D, i) * 1000D);
            levelMemberLimits.put(Long.toString(i), i * 5L);
            levelClaimLimits.put(Long.toString(i), i * 6L);
        }
    }

    @NotNull
    public Map<Long, BigDecimal> getLevelMoneyRequirements() {
        return levelMoneyRequirements.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> Long.parseLong(entry.getKey()),
                        entry -> BigDecimal.valueOf(entry.getValue()).setScale(2, RoundingMode.HALF_UP)
                ));
    }

    @NotNull
    public Map<Long, Long> getLevelMemberLimits() {
        return levelMemberLimits.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> Long.parseLong(entry.getKey()),
                        Map.Entry::getValue
                ));
    }

    @NotNull
    public Map<Long, Long> getLevelClaimLimits() {
        return levelClaimLimits.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> Long.parseLong(entry.getKey()),
                        Map.Entry::getValue
                ));
    }


    public long getMaxClaims(long level) {
        return getLevelClaimLimits().getOrDefault(level, 0L);
    }

    public long getMaxMembers(long level) {
        return getLevelMemberLimits().getOrDefault(level, 0L);
    }

    @NotNull
    public BigDecimal getLevelUpCost(long level) {
        if (level >= getMaxLevel()) {
            return BigDecimal.valueOf(Double.POSITIVE_INFINITY);
        }
        return getLevelMoneyRequirements().getOrDefault(level + 1, BigDecimal.ZERO);
    }

    public long getMaxLevel() {
        return getLevelMoneyRequirements().keySet().stream().max(Long::compareTo).orElse(0L);
    }

}
