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

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.Ignore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Flag;
import net.william278.husktowns.claim.Rules;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@SuppressWarnings("FieldMayBeFinal")
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RulePresets {

    protected static final String CONFIG_HEADER = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃    HuskTowns Rule Presets    ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ This file is for configuring the default flag rule rulePresets within towns and the public rules outside of towns.
        ┗╸ Config Help: https://william278.net/docs/husktowns/config-files""";

    @Comment("Rules for the wilderness (claimable chunks outside of towns)")
    private Map<String, Boolean> wildernessRules = Map.of(
        Flag.Defaults.EXPLOSION_DAMAGE.getName(), true,
        Flag.Defaults.FIRE_DAMAGE.getName(), true,
        Flag.Defaults.MOB_GRIEFING.getName(), true,
        Flag.Defaults.MONSTER_SPAWNING.getName(), true,
        Flag.Defaults.PUBLIC_BUILD_ACCESS.getName(), true,
        Flag.Defaults.PUBLIC_CONTAINER_ACCESS.getName(), true,
        Flag.Defaults.PUBLIC_FARM_ACCESS.getName(), true,
        Flag.Defaults.PUBLIC_INTERACT_ACCESS.getName(), true,
        Flag.Defaults.PVP.getName(), true
    );

    @Comment("Rules for admin claims (created with /admintown claim)")
    private Map<String, Boolean> adminClaimRules = Map.of(
        Flag.Defaults.EXPLOSION_DAMAGE.getName(), false,
        Flag.Defaults.FIRE_DAMAGE.getName(), false,
        Flag.Defaults.MOB_GRIEFING.getName(), false,
        Flag.Defaults.MONSTER_SPAWNING.getName(), false,
        Flag.Defaults.PUBLIC_BUILD_ACCESS.getName(), false,
        Flag.Defaults.PUBLIC_CONTAINER_ACCESS.getName(), false,
        Flag.Defaults.PUBLIC_FARM_ACCESS.getName(), true,
        Flag.Defaults.PUBLIC_INTERACT_ACCESS.getName(), true,
        Flag.Defaults.PVP.getName(), false
    );

    @Comment("Rules for worlds where claims cannot be created (as defined in unclaimable_worlds)")
    private Map<String, Boolean> unclaimableWorldRules = Map.of(
        Flag.Defaults.EXPLOSION_DAMAGE.getName(), true,
        Flag.Defaults.FIRE_DAMAGE.getName(), true,
        Flag.Defaults.MOB_GRIEFING.getName(), true,
        Flag.Defaults.MONSTER_SPAWNING.getName(), true,
        Flag.Defaults.PUBLIC_BUILD_ACCESS.getName(), true,
        Flag.Defaults.PUBLIC_CONTAINER_ACCESS.getName(), true,
        Flag.Defaults.PUBLIC_FARM_ACCESS.getName(), true,
        Flag.Defaults.PUBLIC_INTERACT_ACCESS.getName(), true,
        Flag.Defaults.PVP.getName(), true
    );

    @Comment("Default rules when a town is at war (only used during a town war)")
    private Map<String, Boolean> wartimeRules = Map.of(
        Flag.Defaults.EXPLOSION_DAMAGE.getName(), true,
        Flag.Defaults.FIRE_DAMAGE.getName(), true,
        Flag.Defaults.MOB_GRIEFING.getName(), true,
        Flag.Defaults.MONSTER_SPAWNING.getName(), true,
        Flag.Defaults.PUBLIC_BUILD_ACCESS.getName(), true,
        Flag.Defaults.PUBLIC_CONTAINER_ACCESS.getName(), true,
        Flag.Defaults.PUBLIC_FARM_ACCESS.getName(), true,
        Flag.Defaults.PUBLIC_INTERACT_ACCESS.getName(), true,
        Flag.Defaults.PVP.getName(), true
    );

    @Comment("The default rules for different town claim types")
    @Getter
    private ClaimRulePresets defaultRules = new ClaimRulePresets();

    @Configuration
    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static class ClaimRulePresets {
        @Comment("Default rules for normal claims")
        private Map<String, Boolean> claims = Map.of(
            Flag.Defaults.EXPLOSION_DAMAGE.getName(), false,
            Flag.Defaults.FIRE_DAMAGE.getName(), false,
            Flag.Defaults.MOB_GRIEFING.getName(), false,
            Flag.Defaults.MONSTER_SPAWNING.getName(), true,
            Flag.Defaults.PUBLIC_BUILD_ACCESS.getName(), false,
            Flag.Defaults.PUBLIC_CONTAINER_ACCESS.getName(), false,
            Flag.Defaults.PUBLIC_FARM_ACCESS.getName(), false,
            Flag.Defaults.PUBLIC_INTERACT_ACCESS.getName(), false,
            Flag.Defaults.PVP.getName(), false
        );

        @Comment("Default rules for farm claims")
        private Map<String, Boolean> farms = Map.of(
            Flag.Defaults.EXPLOSION_DAMAGE.getName(), false,
            Flag.Defaults.FIRE_DAMAGE.getName(), false,
            Flag.Defaults.MOB_GRIEFING.getName(), false,
            Flag.Defaults.MONSTER_SPAWNING.getName(), true,
            Flag.Defaults.PUBLIC_BUILD_ACCESS.getName(), false,
            Flag.Defaults.PUBLIC_CONTAINER_ACCESS.getName(), false,
            Flag.Defaults.PUBLIC_FARM_ACCESS.getName(), true,
            Flag.Defaults.PUBLIC_INTERACT_ACCESS.getName(), false,
            Flag.Defaults.PVP.getName(), false
        );

        @Comment("Default rules for plot claims")
        private Map<String, Boolean> plots = Map.of(
            Flag.Defaults.EXPLOSION_DAMAGE.getName(), false,
            Flag.Defaults.FIRE_DAMAGE.getName(), false,
            Flag.Defaults.MOB_GRIEFING.getName(), false,
            Flag.Defaults.MONSTER_SPAWNING.getName(), false,
            Flag.Defaults.PUBLIC_BUILD_ACCESS.getName(), false,
            Flag.Defaults.PUBLIC_CONTAINER_ACCESS.getName(), false,
            Flag.Defaults.PUBLIC_FARM_ACCESS.getName(), false,
            Flag.Defaults.PUBLIC_INTERACT_ACCESS.getName(), false,
            Flag.Defaults.PVP.getName(), false
        );

        @Ignore
        private Rules claimRules;
        @Ignore
        private Rules farmRules;
        @Ignore
        private Rules plotRules;

        @NotNull
        public Rules getClaims(@NotNull Flags flagConfig) {
            return plotRules == null ? plotRules = Rules.from(claims) : plotRules;
        }

        @NotNull
        public Rules getFarms(@NotNull Flags flagConfig) {
            return farmRules == null ? farmRules = Rules.from(farms) : farmRules;
        }

        @NotNull
        public Rules getPlots(@NotNull Flags flagConfig) {
            return claimRules == null ? claimRules = Rules.from(plots) : claimRules;
        }

        @NotNull
        public Map<Claim.Type, Rules> getDefaults(@NotNull Flags flagConfig) {
            return Map.of(
                Claim.Type.CLAIM, getClaims(flagConfig),
                Claim.Type.FARM, getFarms(flagConfig),
                Claim.Type.PLOT, getPlots(flagConfig)
            );
        }
    }

    @Ignore
    private Rules unclaimable;
    @Ignore
    private Rules wilderness;
    @Ignore
    private Rules adminClaims;
    @Ignore
    private Rules wartime;

    @NotNull
    public Rules getUnclaimableWorldRules(@NotNull Flags flagConfig) {
        return unclaimable == null ? unclaimable = Rules.from(unclaimableWorldRules) : unclaimable;
    }

    @NotNull
    public Rules getWildernessRules(@NotNull Flags flagConfig) {
        return wilderness == null ? wilderness = Rules.from(wildernessRules) : wilderness;
    }

    @NotNull
    public Rules getAdminClaimRules(@NotNull Flags flagConfig) {
        return adminClaims == null ? adminClaims = Rules.from(adminClaimRules) : adminClaims;
    }

    @NotNull
    public Rules getWartimeRules(@NotNull Flags flagConfig) {
        return wartime == null ? wartime = Rules.from(wartimeRules) : wartime;
    }

}
