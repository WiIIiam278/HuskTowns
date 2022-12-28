package net.william278.husktowns.config;

import net.william278.annotaml.YamlFile;
import net.william278.husktowns.town.Privilege;
import net.william278.husktowns.town.Role;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃  HuskTowns town role config  ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ This file is for configuring town roles and associated privileges.
        ┣╸ Each role is mapped to a weight, identifying its hierarchical position. Each weight is also mapped to the role name.
        ┗╸ Documentation: https://william278.net/docs/husktowns/town-roles""")
public class Roles {

    @SuppressWarnings("FieldMayBeFinal")
    private Map<Integer, String> names = new LinkedHashMap<>(Map.of(
            3, "Mayor",
            2, "Trustee",
            1, "Resident"
    ));

    // Default role assignments
    @SuppressWarnings("FieldMayBeFinal")
    private Map<Integer, List<String>> roles = new LinkedHashMap<>(Map.of(
            3, List.of(
                    Privilege.SET_BIO.id(),
                    Privilege.EVICT.id(),
                    Privilege.PROMOTE.id(),
                    Privilege.DEMOTE.id(),
                    Privilege.WITHDRAW.id(),
                    Privilege.SET_RULES.id(),
                    Privilege.RENAME.id(),
                    Privilege.SET_COLOR.id()),
            2, List.of(
                    Privilege.SET_FARM.id(),
                    Privilege.SET_PLOT.id(),
                    Privilege.ADD_PLOT_MEMBERS.id(),
                    Privilege.TRUSTED_ACCESS.id(),
                    Privilege.UNCLAIM.id(),
                    Privilege.CLAIM.id(),
                    Privilege.SET_GREETING.id(),
                    Privilege.SET_FAREWELL.id(),
                    Privilege.INVITE.id(),
                    Privilege.SET_SPAWN.id(),
                    Privilege.SPAWN_PRIVACY.id(),
                    Privilege.VIEW_LOGS.id()),
            1, List.of(
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
     * @return the town roles map
     * @throws IllegalStateException if the roles map is invalid
     */
    @NotNull
    public List<Role> getRoles() throws IllegalStateException {
        final ArrayList<Role> roleList = new ArrayList<>();
        for (final Map.Entry<Integer, List<String>> roleMapping : roles.entrySet()) {
            final int weight = roleMapping.getKey();
            final List<Privilege> privileges = roleMapping.getValue().stream().map(Privilege::fromId).toList();
            roleList.add(Role.of(weight, getName(weight), privileges));
        }
        return roleList;
    }

    @NotNull
    private String getName(int weight) throws IllegalStateException {
        if (!names.containsKey(weight)) {
            throw new IllegalStateException("Invalid roles.yml file: Weight " + weight + " does not have a name assigned");
        }
        return names.get(weight);
    }

    @NotNull
    public Role getMayor() {
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

    public Optional<Role> fromWeight(int weight) {
        return getRoles().stream()
                .filter(role -> role.getWeight() == weight)
                .findFirst();
    }

}
