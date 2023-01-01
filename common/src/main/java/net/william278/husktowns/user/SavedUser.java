package net.william278.husktowns.user;

import org.jetbrains.annotations.NotNull;

public record SavedUser(@NotNull User user, @NotNull Preferences preferences) {

    public static SavedUser create(@NotNull User user) {
        return new SavedUser(user, Preferences.getDefaults());
    }

}
