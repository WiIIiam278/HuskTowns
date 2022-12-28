package net.william278.husktowns.hook;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public abstract class EconomyHook extends Hook {

    protected EconomyHook(@NotNull HuskTowns plugin, @NotNull String name) {
        super(plugin, name);
    }

    public abstract boolean hasMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount);

    public abstract void takeMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount);

    public abstract void giveMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount);

    public abstract String formatMoney(@NotNull BigDecimal amount);

}
