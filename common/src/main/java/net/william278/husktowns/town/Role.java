package net.william278.husktowns.town;

import net.william278.husktowns.HuskTowns;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Role {

    private int weight;
    private String name;
    private List<Privilege> privileges;

    private Role(int weight, @NotNull String name, @NotNull List<Privilege> privileges) {
        this.weight = weight;
        this.name = name;
        this.privileges = privileges;
    }

    public static Role of(int weight, @NotNull String name, @NotNull List<Privilege> privileges) {
        return new Role(weight, name, privileges);
    }

    @SuppressWarnings("unused")
    private Role() {
    }

    public int getWeight() {
        return weight;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public List<Privilege> getPrivileges() {
        return privileges;
    }

    /**
     * Returns if the role has the specified privilege, including inherited privileges from parent roles
     *
     * @param plugin    the HuskTowns plugin instance
     * @param privilege the privilege to check
     * @return {@code true} if the role has the specified privilege; {@code false} otherwise
     */
    public boolean hasPrivilege(@NotNull HuskTowns plugin, @NotNull Privilege privilege) {
        return getPrivileges().contains(privilege) || plugin.getRoles().fromWeight(getWeight() - 1)
                .map(role -> role.hasPrivilege(plugin, privilege))
                .orElse(false);
    }
}
