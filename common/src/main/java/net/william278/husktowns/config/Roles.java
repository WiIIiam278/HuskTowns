package net.william278.husktowns.config;

import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import net.william278.husktowns.town.Privilege;
import net.william278.husktowns.town.Role;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃        HuskTowns Roles       ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ This file is for configuring town roles and privileges.
        ┗╸ Documentation: https://william278.net/docs/husktowns/town-roles""",
        rootedMap = true)
public class Roles {

    private Map<String, Map<String, ?>> roles = Map.of(
            "mayor", Map.of(
                    "weight", 3,
                    "name", "Mayor",
                    "privileges", List.of()),
            "trustee", Map.of(
                    "weight", 2,
                    "name", "Trustee",
                    "privileges", List.of()),
            "resident", Map.of(
                    "weight", 1,
                    "name", "Resident",
                    "privileges", List.of())
    );

    private Roles(@NotNull Map<String, Map<String, ?>> roles) {
        this.roles = roles;
    }

    @SuppressWarnings("unused")
    private Roles() {
    }

    /**
     * Get the town roles map
     *
     * @return the town roles map
     * @throws IllegalArgumentException if the roles map is invalid
     */
    @SuppressWarnings("unchecked")
    public List<Role> getRoles() throws IllegalArgumentException {
        return roles.entrySet().stream().map(entry -> {
            final String id = entry.getKey();
            final Map<String, ?> role = entry.getValue();
            return Role.of((int) role.get("weight"), id, (String) role.get("name"),
                    ((List<String>) role.get("privileges")).stream()
                            .map(String::toUpperCase)
                            .map(Privilege::valueOf)
                            .toList());
        }).toList();
    }

}
