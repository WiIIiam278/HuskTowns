package net.william278.husktowns.config;

import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
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
    private Map<String, BigDecimal> levelMoneyRequirements = new LinkedHashMap<>();

    @YamlKey("level_member_limits")
    private Map<String, Integer> levelMemberLimits = new LinkedHashMap<>();

    @YamlKey("level_claim_limits")
    private Map<String, Integer> levelClaimLimits = new LinkedHashMap<>();

    public Levels() {
        for (int i = 0; i < 20; i++) {
            levelMoneyRequirements.put(Integer.toString(i), BigDecimal.valueOf(Math.pow(2D, i) * 1000D));
            levelMemberLimits.put(Integer.toString(i), i * 5);
            levelClaimLimits.put(Integer.toString(i), i * 6);
        }
    }

    @NotNull
    public Map<Integer, BigDecimal> getLevelMoneyRequirements() {
        return levelMoneyRequirements.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> Integer.parseInt(entry.getKey()),
                        entry -> entry.getValue().setScale(2, RoundingMode.HALF_UP)
                ));
    }

    @NotNull
    public Map<Integer, Integer> getLevelMemberLimits() {
        return levelMemberLimits.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> Integer.parseInt(entry.getKey()),
                        Map.Entry::getValue
                ));
    }

    @NotNull
    public Map<Integer, Integer> getLevelClaimLimits() {
        return levelClaimLimits.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> Integer.parseInt(entry.getKey()),
                        Map.Entry::getValue
                ));
    }


    public int getMaxClaims(int level) {
        return getLevelClaimLimits().getOrDefault(level, 0);
    }

    public int getMaxMembers(int level) {
        return getLevelMemberLimits().getOrDefault(level, 0);
    }

    @NotNull
    public BigDecimal getLevelUpCost(int level) {
        if (level >= getMaxLevel()) {
            return BigDecimal.valueOf(Double.POSITIVE_INFINITY);
        }
        return getLevelMoneyRequirements().getOrDefault(level + 1, BigDecimal.ZERO);
    }

    public long getMaxLevel() {
        return getLevelMoneyRequirements().keySet().stream().max(Integer::compareTo).orElse(0);
    }

}
