package net.william278.husktowns.hook;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public abstract class EconomyHook extends Hook {

    protected EconomyHook(@NotNull HuskTowns plugin, @NotNull String name) {
        super(plugin, name);
    }

    public abstract boolean hasMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount);

    /**
     * Take money from a player
     * @param user The player to take money from
     * @param amount The amount of money to take
     * @param reason The reason for taking money
     * @return Whether the transaction was successful, false if the player does not have enough money
     */
    public abstract boolean takeMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount, @Nullable String reason);

    public abstract void giveMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount, @Nullable String reason);

    public abstract String formatMoney(@NotNull BigDecimal amount);

}
