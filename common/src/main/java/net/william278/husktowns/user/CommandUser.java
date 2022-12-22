package net.william278.husktowns.user;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

public interface CommandUser {

    @NotNull
    Audience getAudience();

    boolean hasPermission(@NotNull String permission);

    default void sendMessage(@NotNull MineDown mineDown) {
        getAudience().sendMessage(mineDown.toComponent());
    }

}
