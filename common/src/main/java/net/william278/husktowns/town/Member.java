package net.william278.husktowns.town;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the member of a town
 *
 * @param user The {@link User}
 * @param town The {@link Town} they are a member of
 * @param role The {@link Role} the user has in the town
 */
public record Member(@NotNull User user, @NotNull Town town, @NotNull Role role) {

    /**
     * Get whether the member has the provided privilege in the town
     *
     * @param plugin    The HuskTowns plugin instance
     * @param privilege The {@link Privilege} to check
     * @return {@code true} if the member has the privilege, {@code false} otherwise
     */
    public boolean hasPrivilege(@NotNull HuskTowns plugin, @NotNull Privilege privilege) {
        return role().hasPrivilege(plugin, privilege);
    }

}
