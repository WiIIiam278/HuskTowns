package net.william278.husktowns.hook;

import dev.unnm3d.rediseconomy.api.RedisEconomyAPI;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.logging.Level;

public class RedisEconomyHook extends EconomyHook {

    private RedisEconomyAPI redisEconomy;

    public RedisEconomyHook(@NotNull HuskTowns plugin) {
        super(plugin, "RedisEconomy");
    }

    @Override
    public void onEnable() {
        this.redisEconomy = RedisEconomyAPI.getAPI();
        plugin.log(Level.INFO, "Enabled RedisEconomy hook");
    }

    @NotNull
    public BigDecimal getBalance(@NotNull OnlineUser user) {
        return BigDecimal.valueOf(redisEconomy.getDefaultCurrency().getBalance(user.getUuid()));
    }

    @Override
    public boolean takeMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount, @Nullable String reason) {
        final BigDecimal changeBy = getBalance(user).subtract(amount);
        return redisEconomy.getDefaultCurrency().withdrawPlayer(user.getUuid(), user.getUsername(), changeBy.doubleValue(), reason).transactionSuccess();
    }

    @Override
    public void giveMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount, @Nullable String reason) {
        final BigDecimal changeBy = getBalance(user).add(amount);
        redisEconomy.getDefaultCurrency().depositPlayer(user.getUuid(), user.getUsername(), changeBy.doubleValue(), reason);
    }

    @Override
    public String formatMoney(@NotNull BigDecimal amount) {
        return redisEconomy.getDefaultCurrency().format(amount.doubleValue());
    }

}
