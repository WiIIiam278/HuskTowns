package net.william278.husktowns.hook;

import dev.unnm3d.rediseconomy.api.RedisEconomyAPI;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class RedisEconomyHook extends EconomyHook {

    private RedisEconomyAPI api;

    public RedisEconomyHook(@NotNull HuskTowns plugin) {
        super(plugin, "RedisEconomy");
    }

    @Override
    public void onEnable() {
        this.api = RedisEconomyAPI.getAPI();
    }

    @NotNull
    public BigDecimal getBalance(@NotNull OnlineUser user) {
        return BigDecimal.valueOf(api.getDefaultCurrency().getBalance(user.getUuid()));
    }

    @Override
    public boolean hasMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount) {
        return getBalance(user).compareTo(amount) >= 0;
    }

    @Override
    public void takeMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount) {
        final BigDecimal changeBy = getBalance(user).subtract(amount);
        api.getDefaultCurrency().setPlayerBalance(user.getUuid(), user.getUsername(), changeBy.doubleValue());
    }

    @Override
    public void giveMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount) {
        final BigDecimal changeBy = getBalance(user).add(amount);
        api.getDefaultCurrency().setPlayerBalance(user.getUuid(), user.getUsername(), changeBy.doubleValue());
    }

    @Override
    public String formatMoney(@NotNull BigDecimal amount) {
        return api.getDefaultCurrency().format(amount.doubleValue());
    }

}
