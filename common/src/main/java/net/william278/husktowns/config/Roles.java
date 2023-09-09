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
import net.william278.husktowns.town.Privilege;
import net.william278.husktowns.town.Role;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃  HuskTowns town role config  ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ This file is for configuring town roles and associated privileges.
        ┣╸ Each role is mapped to a weight, identifying its hierarchical position. Each weight is also mapped to the role name.
        ┣╸ Config Help: https://william278.net/docs/husktowns/config-files
        ┗╸ Documentation: https://william278.net/docs/husktowns/town-roles""")
public class Roles {

    @YamlComment("Map of role weight IDs to display names")
    @YamlKey("names")
    private Map<String, String> names = new LinkedHashMap<>(Map.of(
            "3", "Mayor",
            "2", "Trustee",
            "1", "Resident"
    ));

    @YamlComment("""
            Map of role weight IDs to privileges
            The format is roleID-townLevel""")
    @YamlKey("roles")
    private Map<String, List<String>> roles = new LinkedHashMap<>(Map.of(
            "3", List.of(
                    Privilege.SET_BIO.id(),
                    Privilege.EVICT.id(),
                    Privilege.PROMOTE.id(),
                    Privilege.DEMOTE.id(),
                    Privilege.WITHDRAW.id(),
                    Privilege.LEVEL_UP.id(),
                    Privilege.SET_RULES.id(),
                    Privilege.RENAME.id(),
                    Privilege.SET_COLOR.id()),
            "3-1", List.of(
                    Privilege.EVICT.id(),
                    Privilege.LEVEL_UP.id(),
                    Privilege.RENAME.id()),
            "2", List.of(
                    Privilege.SET_FARM.id(),
                    Privilege.SET_PLOT.id(),
                    Privilege.MANAGE_PLOT_MEMBERS.id(),
                    Privilege.TRUSTED_ACCESS.id(),
                    Privilege.UNCLAIM.id(),
                    Privilege.CLAIM.id(),
                    Privilege.SET_GREETING.id(),
                    Privilege.SET_FAREWELL.id(),
                    Privilege.INVITE.id(),
                    Privilege.SET_SPAWN.id(),
                    Privilege.SPAWN_PRIVACY.id(),
                    Privilege.VIEW_LOGS.id()),
            "1", List.of(
                    Privilege.DEPOSIT.id(),
                    Privilege.CHAT.id(),
                    Privilege.SPAWN.id())
    ));

    @SuppressWarnings("unused")
    private Roles() {
    }

    /**
     * Get the town roles map
     *
     * @param town the town to get the roles for
     * @return the town roles map
     * @throws IllegalStateException if the role map is invalid
     */
    @NotNull
    public List<Role> getRoles(Town town) throws IllegalStateException {
        final HashMap<Integer, Role> weightRoleMap = new HashMap<>();
        for (final Map.Entry<String, List<String>> roleMapping : roles.entrySet()) {

            final String[] splitID = roleMapping.getKey().split("-");
            final int weight = Integer.parseInt(splitID[0]);
            final Integer roleTownLevel = splitID.length == 1 ? null : Integer.parseInt(splitID[1]);

            // Skip if the role is for a different town level
            if (town != null && roleTownLevel != null) {
                if (town.getLevel() != roleTownLevel) {
                    continue;
                }
            }

            final List<Privilege> privileges = roleMapping.getValue().stream().map(Privilege::fromId).toList();
            weightRoleMap.compute(weight, (key, existingRole) -> {
                // If the role already exists, overwrite it if there is a town level specified
                if (existingRole != null && roleTownLevel == null) {
                    return existingRole;
                }
                return Role.of(weight, getName(weight), privileges);
            });
        }
        return weightRoleMap.values().stream().toList();
    }

    /**
     * Get the town roles map
     *
     * @return the town roles map
     * @throws IllegalStateException if the role map is invalid
     */
    @NotNull
    public List<Role> getRoles() throws IllegalStateException {
        return getRoles(null);
    }


    @NotNull
    private String getName(int weight) throws IllegalStateException {
        if (!names.containsKey(Integer.toString(weight))) {
            throw new IllegalStateException("Invalid roles.yml file: Weight " + weight + " does not have a name assigned");
        }
        return names.get(Integer.toString(weight));
    }

    @NotNull
    public Role getMayorRole() {
        return getRoles().stream()
                .max(Comparator.comparingInt(Role::getWeight))
                .orElseThrow();
    }

    @NotNull
    public Role getDefaultRole() {
        return getRoles().stream()
                .min(Comparator.comparingInt(Role::getWeight))
                .orElseThrow();
    }

    public Optional<Role> fromWeight(int weight, Town town) {
        return getRoles(town).stream()
                .filter(role -> role.getWeight() == weight)
                .findFirst();
    }

    public Optional<Role> fromWeight(int weight) {
        return fromWeight(weight, null);
    }

}
