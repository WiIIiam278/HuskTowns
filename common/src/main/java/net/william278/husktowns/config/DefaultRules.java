package net.william278.husktowns.config;

import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Flag;
import net.william278.husktowns.claim.Rules;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Collectors;

@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃    HuskTowns default rules   ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ This file is for configuring the default flag rules within towns and the public rules outside of towns.
        ┗╸ Documentation: https://william278.net/docs/husktowns/claim-rules""")
public class DefaultRules {
    @YamlKey("default_rules")
    private Map<String, Map<String, Boolean>> defaultRules = Map.of(
            Claim.Type.CLAIM.name().toLowerCase(), Map.of(
                    Flag.EXPLOSION_DAMAGE.name().toLowerCase(), false,
                    Flag.FIRE_DAMAGE.name().toLowerCase(), false,
                    Flag.MOB_GRIEFING.name().toLowerCase(), false,
                    Flag.MONSTER_SPAWNING.name().toLowerCase(), true,
                    Flag.PUBLIC_BUILD_ACCESS.name().toLowerCase(), false,
                    Flag.PUBLIC_CONTAINER_ACCESS.name().toLowerCase(), false,
                    Flag.PUBLIC_FARM_ACCESS.name().toLowerCase(), false,
                    Flag.PUBLIC_INTERACT_ACCESS.name().toLowerCase(), false,
                    Flag.PVP.name().toLowerCase(), false
            ),
            Claim.Type.FARM.name().toLowerCase(), Map.of(
                    Flag.EXPLOSION_DAMAGE.name().toLowerCase(), false,
                    Flag.FIRE_DAMAGE.name().toLowerCase(), false,
                    Flag.MOB_GRIEFING.name().toLowerCase(), false,
                    Flag.MONSTER_SPAWNING.name().toLowerCase(), true,
                    Flag.PUBLIC_BUILD_ACCESS.name().toLowerCase(), false,
                    Flag.PUBLIC_CONTAINER_ACCESS.name().toLowerCase(), false,
                    Flag.PUBLIC_FARM_ACCESS.name().toLowerCase(), true,
                    Flag.PUBLIC_INTERACT_ACCESS.name().toLowerCase(), false,
                    Flag.PVP.name().toLowerCase(), false
            ),
            Claim.Type.PLOT.name().toLowerCase(), Map.of(
                    Flag.EXPLOSION_DAMAGE.name().toLowerCase(), false,
                    Flag.FIRE_DAMAGE.name().toLowerCase(), false,
                    Flag.MOB_GRIEFING.name().toLowerCase(), false,
                    Flag.MONSTER_SPAWNING.name().toLowerCase(), false,
                    Flag.PUBLIC_BUILD_ACCESS.name().toLowerCase(), false,
                    Flag.PUBLIC_CONTAINER_ACCESS.name().toLowerCase(), false,
                    Flag.PUBLIC_FARM_ACCESS.name().toLowerCase(), false,
                    Flag.PUBLIC_INTERACT_ACCESS.name().toLowerCase(), false,
                    Flag.PVP.name().toLowerCase(), false
            )
    );

    @YamlKey("wilderness_rules")
    private Map<String, Boolean> wildernessRules = Map.of(
            Flag.EXPLOSION_DAMAGE.name().toLowerCase(), true,
            Flag.FIRE_DAMAGE.name().toLowerCase(), true,
            Flag.MOB_GRIEFING.name().toLowerCase(), true,
            Flag.MONSTER_SPAWNING.name().toLowerCase(), true,
            Flag.PUBLIC_BUILD_ACCESS.name().toLowerCase(), true,
            Flag.PUBLIC_CONTAINER_ACCESS.name().toLowerCase(), true,
            Flag.PUBLIC_FARM_ACCESS.name().toLowerCase(), true,
            Flag.PUBLIC_INTERACT_ACCESS.name().toLowerCase(), true,
            Flag.PVP.name().toLowerCase(), true
    );

    @YamlKey("admin_claim_rules")
    private Map<String, Boolean> adminClaimRules = Map.of(
            Flag.EXPLOSION_DAMAGE.name().toLowerCase(), false,
            Flag.FIRE_DAMAGE.name().toLowerCase(), false,
            Flag.MOB_GRIEFING.name().toLowerCase(), false,
            Flag.MONSTER_SPAWNING.name().toLowerCase(), false,
            Flag.PUBLIC_BUILD_ACCESS.name().toLowerCase(), false,
            Flag.PUBLIC_CONTAINER_ACCESS.name().toLowerCase(), false,
            Flag.PUBLIC_FARM_ACCESS.name().toLowerCase(), true,
            Flag.PUBLIC_INTERACT_ACCESS.name().toLowerCase(), true,
            Flag.PVP.name().toLowerCase(), false
    );

    @YamlKey("unclaimable_world_rules")
    private Map<String, Boolean> unclaimableWorldRules = Map.of(
            Flag.EXPLOSION_DAMAGE.name().toLowerCase(), true,
            Flag.FIRE_DAMAGE.name().toLowerCase(), true,
            Flag.MOB_GRIEFING.name().toLowerCase(), true,
            Flag.MONSTER_SPAWNING.name().toLowerCase(), true,
            Flag.PUBLIC_BUILD_ACCESS.name().toLowerCase(), true,
            Flag.PUBLIC_CONTAINER_ACCESS.name().toLowerCase(), true,
            Flag.PUBLIC_FARM_ACCESS.name().toLowerCase(), true,
            Flag.PUBLIC_INTERACT_ACCESS.name().toLowerCase(), true,
            Flag.PVP.name().toLowerCase(), true
    );

    @NotNull
    public Map<Claim.Type, Rules> getDefaultClaimRules() {
        return defaultRules.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> Claim.Type.fromId(entry.getKey()).orElseThrow(),
                        entry -> Rules.of(entry.getValue().entrySet().stream()
                                .collect(Collectors.toMap(
                                        flagEntry -> Flag.fromId(flagEntry.getKey()).orElseThrow(),
                                        Map.Entry::getValue
                                )))
                ));
    }

    @NotNull
    public Rules getUnclaimableWorldRules() {
        return Rules.of(unclaimableWorldRules.entrySet().stream()
                .collect(Collectors.toMap(e -> Flag.fromId(e.getKey()).orElseThrow(), Map.Entry::getValue)));
    }

    @NotNull
    public Rules getWildernessRules() {
        return Rules.of(wildernessRules.entrySet().stream()
                .collect(Collectors.toMap(e -> Flag.fromId(e.getKey()).orElseThrow(), Map.Entry::getValue)));
    }

    @NotNull
    public Rules getAdminClaimRules() {
        return Rules.of(adminClaimRules.entrySet().stream()
                .collect(Collectors.toMap(e -> Flag.fromId(e.getKey()).orElseThrow(), Map.Entry::getValue)));
    }

    @SuppressWarnings("unused")
    public DefaultRules() {
    }

}
