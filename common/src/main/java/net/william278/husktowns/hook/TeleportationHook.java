package net.william278.husktowns.hook;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

public abstract class TeleportationHook extends Hook {
    protected TeleportationHook(@NotNull HuskTowns plugin, @NotNull String name) {
        super(plugin, name);
    }

    public abstract void teleport(@NotNull OnlineUser user, @NotNull Position position, @NotNull String server);

}
