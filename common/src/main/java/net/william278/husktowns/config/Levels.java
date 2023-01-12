package net.william278.husktowns.config;

import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
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
    private Map<String, Double> levelMoneyRequirements = new LinkedHashMap<>();

    @YamlKey("level_member_limits")
    private Map<String, Integer> levelMemberLimits = new LinkedHashMap<>();

    @YamlKey("level_claim_limits")
    private Map<String, Integer> levelClaimLimits = new LinkedHashMap<>();

    @YamlKey("level_crop_growth_rate_bonus")
    private Map<String, Double> levelCropGrowthRateBonus = new LinkedHashMap<>();

    @YamlKey("level_mob_spawner_rate_bonus")
    private Map<String, Double> levelMobSpawnerRateBonus = new LinkedHashMap<>();

    public Levels() {
        for (int i = 1; i <= 20; i++) {
            levelMoneyRequirements.put(Integer.toString(i), Math.pow(2D, i) * 1000D);
            levelMemberLimits.put(Integer.toString(i), i * 5);
            levelClaimLimits.put(Integer.toString(i), i * 6);
            levelCropGrowthRateBonus.put(Integer.toString(i), 0.075 * i);
            levelMobSpawnerRateBonus.put(Integer.toString(i), 0.05 * i);
        }
    }

    @NotNull
    private Map<Integer, BigDecimal> getLevelMoneyRequirements() {
        return levelMoneyRequirements.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> Integer.parseInt(entry.getKey()),
                        entry -> BigDecimal.valueOf(entry.getValue()).setScale(2, RoundingMode.HALF_UP)
                ));
    }

    @NotNull
    private Map<Integer, Integer> getLevelMemberLimits() {
        return levelMemberLimits.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> Integer.parseInt(entry.getKey()),
                        Map.Entry::getValue
                ));
    }

    @NotNull
    private Map<Integer, Integer> getLevelClaimLimits() {
        return levelClaimLimits.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> Integer.parseInt(entry.getKey()),
                        Map.Entry::getValue
                ));
    }

    @NotNull
    private Map<Integer, Double> getLevelCropGrowthRateBonus() {
        return levelCropGrowthRateBonus.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> Integer.parseInt(entry.getKey()),
                        Map.Entry::getValue
                ));
    }

    @NotNull
    private Map<Integer, Double> getLevelMobSpawnerRateBonus() {
        return levelMobSpawnerRateBonus.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> Integer.parseInt(entry.getKey()),
                        Map.Entry::getValue
                ));
    }


    /**
     * Get the maximum number of claims a town of a certain level can make
     *
     * @param level the level
     * @return the maximum number of claims a town of that level can make
     */
    public int getMaxClaims(int level) {
        return getLevelClaimLimits().getOrDefault(Math.min(level, getMaxLevel()), 0);
    }

    /**
     * Get the maximum number of members a town of a certain level can have
     *
     * @param level the level
     * @return the maximum number of members a town of that level can make
     */
    public int getMaxMembers(int level) {
        return getLevelMemberLimits().getOrDefault(Math.min(level, getMaxLevel()), 0);
    }

    /**
     * Get the bonus crop growth rate a town of a certain level has
     *
     * @param level the level
     * @return the bonus crop growth rate a town of that level has
     */
    public double getCropGrowthRateBonus(int level) {
        return getLevelCropGrowthRateBonus().getOrDefault(Math.min(level, getMaxLevel()), 0D);
    }

    /**
     * Get the bonus mob spawner rate a town of a certain level has
     *
     * @param level the level
     * @return the bonus mob spawner rate a town of that level has
     */
    public double getMobSpawnerRateBonus(int level) {
        return getLevelMobSpawnerRateBonus().getOrDefault(Math.min(level, getMaxLevel()), 0D);
    }

    /**
     * Get the cost required to reach a certain level
     *
     * @param level the next level
     * @return the cost to upgrade from {@code level - 1} to {@code level}
     */
    @NotNull
    public BigDecimal getLevelUpCost(int level) {
        if (level >= getMaxLevel()) {
            return BigDecimal.valueOf(Double.POSITIVE_INFINITY);
        }
        return getLevelMoneyRequirements().getOrDefault(level + 1, BigDecimal.ZERO);
    }

    /**
     * Get the maximum level attainable
     *
     * @return the maximum level attainable
     */
    public int getMaxLevel() {
        return getLevelMoneyRequirements().keySet().stream().max(Integer::compareTo).orElse(0);
    }

    /**
     * Get the highest level a town could upgrade to for the provided money balance
     *
     * @param balance the balance
     * @return the highest level affordable for that amount
     */
    public int getHighestLevelFor(@NotNull BigDecimal balance) {
        BigDecimal currentCost = BigDecimal.ZERO;
        for (int i = 1; i <= getMaxLevel(); i++) {
            currentCost = currentCost.add(getLevelUpCost(i));
            if (currentCost.compareTo(balance) > 0) {
                return i - 1;
            }
        }
        return getMaxLevel();
    }

    /**
     * Returns the total cost needed to level a town up to the supplied level
     *
     * @param level the target level
     * @return the total cost needed to reach that level
     */
    @NotNull
    public BigDecimal getTotalCostFor(int level) {
        return getLevelMoneyRequirements().entrySet().stream()
                .filter(entry -> entry.getKey() <= level)
                .map(Map.Entry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
