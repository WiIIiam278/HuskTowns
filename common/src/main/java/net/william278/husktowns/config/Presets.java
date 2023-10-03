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

import net.william278.annotaml.YamlComment;
import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Flag;
import net.william278.husktowns.claim.Rules;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃    HuskTowns Rule Presets    ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ This file is for configuring the default flag rule presets within towns and the public rules outside of towns.
        ┗╸ Config Help: https://william278.net/docs/husktowns/config-files""")
public class Presets {

    @YamlComment("Rules for the wilderness (claimable chunks outside of towns)")
    @YamlKey("wilderness_rules")
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

    @YamlComment("Rules for admin claims (created with /admintown claim)")
    @YamlKey("admin_claim_rules")
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

    @YamlComment("Rules for worlds where claims cannot be created (as defined in unclaimable_worlds)")
    @YamlKey("unclaimable_world_rules")
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

    @YamlComment("Default rules for normal claims")
    @YamlKey("default_rules.claims")
    private Map<String, Boolean> claimRules = Map.of(
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

    @YamlComment("Default rules for farm claims")
    @YamlKey("default_rules.farms")
    private Map<String, Boolean> farmRules = Map.of(
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

    @YamlComment("Default rules for plot claims")
    @YamlKey("default_rules.plots")
    private Map<String, Boolean> plotRules = Map.of(
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

    @YamlComment("Default rules when a town is at war (only used during a town war)")
    @YamlKey("wartime_rules")
    private Map<String, Boolean> warRules = Map.of(
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

    @SuppressWarnings("unused")
    public Presets() {
    }

    @NotNull
    public Map<Claim.Type, Rules> getDefaultClaimRules() {
        final HashMap<Claim.Type, Map<String, Boolean>> defaultRules = new HashMap<>();
        defaultRules.put(Claim.Type.CLAIM, claimRules);
        defaultRules.put(Claim.Type.FARM, farmRules);
        defaultRules.put(Claim.Type.PLOT, plotRules);
        return defaultRules.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Rules.from(entry.getValue())
                ));
    }

    @NotNull
    public Rules getUnclaimableWorldRules() {
        return Rules.from(unclaimableWorldRules);
    }

    @NotNull
    public Rules getWildernessRules() {
        return Rules.from(wildernessRules);
    }

    @NotNull
    public Rules getAdminClaimRules() {
        return Rules.from(adminClaimRules);
    }

    @NotNull
    public Rules getWarRules() {
        return Rules.from(warRules);
    }

}
