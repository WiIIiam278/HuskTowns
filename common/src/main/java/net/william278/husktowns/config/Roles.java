package net.william278.husktowns.config;

import net.william278.annotaml.YamlFile;
import net.william278.husktowns.town.Privilege;
import net.william278.husktowns.town.Role;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃  HuskTowns town role config  ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ This file is for configuring town roles and associated privileges.
        ┗╸ Documentation: https://william278.net/docs/husktowns/town-roles""",
        rootedMap = true)
public class Roles {

    // Default role assignments
    private Map<String, Map<String, ?>> roles = Map.of(
            "mayor", Map.of(
                    "weight", 3,
                    "name", "Mayor",
                    "privileges", List.of(
                            Privilege.UNASSIGN_PLOT.id(),
                            Privilege.SET_BIO.id(),
                            Privilege.EVICT.id(),
                            Privilege.PROMOTE.id(),
                            Privilege.DEMOTE.id(),
                            Privilege.FLAG.id(),
                            Privilege.RENAME.id())),
            "trustee", Map.of(
                    "weight", 2,
                    "name", "Trustee",
                    "privileges", List.of(
                            Privilege.SET_FARM.id(),
                            Privilege.SET_PLOT.id(),
                            Privilege.ASSIGN_PLOT.id(),
                            Privilege.TRUSTED_ACCESS.id(),
                            Privilege.UNCLAIM.id(),
                            Privilege.CLAIM.id(),
                            Privilege.SET_GREETING.id(),
                            Privilege.SET_FAREWELL.id(),
                            Privilege.INVITE.id(),
                            Privilege.SET_SPAWN.id(),
                            Privilege.SPAWN_PRIVACY.id())),
            "resident", Map.of(
                    "weight", 1,
                    "name", "Resident",
                    "privileges", List.of(
                            Privilege.DEPOSIT.id(),
                            Privilege.CHAT.id(),
                            Privilege.SPAWN.id()))
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
     * @throws IllegalStateException if the roles map is invalid
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public List<Role> getRoles() throws IllegalStateException {
        return List.of(
                Role.of(3, "mayor", "Mayor", List.of(
                        Privilege.UNASSIGN_PLOT,
                        Privilege.SET_BIO,
                        Privilege.EVICT,
                        Privilege.PROMOTE,
                        Privilege.DEMOTE,
                        Privilege.FLAG,
                        Privilege.RENAME,
                        Privilege.SET_COLOR)),
                Role.of(2, "trustee", "Trustee", List.of(
                        Privilege.SET_FARM,
                        Privilege.SET_PLOT,
                        Privilege.ASSIGN_PLOT,
                        Privilege.TRUSTED_ACCESS,
                        Privilege.UNCLAIM,
                        Privilege.CLAIM,
                        Privilege.SET_GREETING,
                        Privilege.SET_FAREWELL,
                        Privilege.INVITE,
                        Privilege.SET_SPAWN,
                        Privilege.SPAWN_PRIVACY)),
                Role.of(1, "resident", "Resident", List.of(
                        Privilege.DEPOSIT,
                        Privilege.CHAT,
                        Privilege.SPAWN))
        );
        /*return roles.entrySet().stream().map(entry -> {
            final String id = entry.getKey();
            final Map<String, ?> roleMap = entry.getValue();
            final int weight = (int) roleMap.get("weight");
            final String name = (String) roleMap.get("name");
            final List<String> privileges = (List<String>) roleMap.get("privileges");
            return Role.of(weight, id, name, privileges.stream().map(Privilege::fromId).toList());
        }).sorted(Comparator.comparingInt(Role::getWeight)).toList();*/
    }

    @NotNull
    public Role getMayor() {
        return getRoles().stream()
                .max(Comparator.comparingInt(Role::getWeight))
                .orElseThrow();
    }

    public Optional<Role> fromWeight(int weight) {
        return getRoles().stream()
                .filter(role -> role.getWeight() == weight)
                .findFirst();
    }
}
