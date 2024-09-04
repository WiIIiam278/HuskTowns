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

package net.william278.husktowns.config;

import com.google.common.collect.Maps;
import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class Levels {

    protected static final String CONFIG_HEADER = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃    HuskTowns Levels Config   ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ This file is for configuring town level requirements and rewards
        ┗╸ Config Help: https://william278.net/docs/husktowns/config-files""";

    @Comment("The amount of money required to level up towns. The Level 1 cost will be taken to create a town if require_first_level_collateral is enabled in config.yml.")
    private LinkedHashMap<String, Double> levelMoneyRequirements = Maps.newLinkedHashMap();

    @Comment("The maximum number of members a town can have at each level")
    private LinkedHashMap<String, Integer> levelMemberLimits = Maps.newLinkedHashMap();

    @Comment("The maximum number of claims a town can have at each level")
    private LinkedHashMap<String, Integer> levelClaimLimits = Maps.newLinkedHashMap();

    @Comment("The bonus crop growth rate percentage a town has at each level (e.g. 105 is 5% faster crop growth)")
    private LinkedHashMap<String, Double> levelCropGrowthRateBonus = Maps.newLinkedHashMap();

    @Comment("The bonus mob spawner rate percentage a town has at each level")
    private LinkedHashMap<String, Double> levelMobSpawnerRateBonus = Maps.newLinkedHashMap();

    public Levels() {
        for (int i = 1; i <= 20; i++) {
            levelMoneyRequirements.put(Integer.toString(i), Math.pow(2D, i) * 1000D);
            levelMemberLimits.put(Integer.toString(i), i * 5);
            levelClaimLimits.put(Integer.toString(i), i * 6);
            levelCropGrowthRateBonus.put(Integer.toString(i), 100d + (i * 5));
            levelMobSpawnerRateBonus.put(Integer.toString(i), 100d + (i * 2.5));
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
     * @param level the current level of the town
     * @return the cost to upgrade from {@code level} to {@code level + 1}
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
