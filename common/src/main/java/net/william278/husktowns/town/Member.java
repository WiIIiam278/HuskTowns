package net.william278.husktowns.town;

import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

public record Member(@NotNull User user, @NotNull Town town, @NotNull Role role) {

}
