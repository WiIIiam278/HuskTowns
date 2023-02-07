package net.william278.husktowns.hook;

import net.milkbowl.vault.economy.Economy;
import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.logging.Level;

public class VaultEconomyHook extends EconomyHook {

    protected Economy economy;

    public VaultEconomyHook(@NotNull HuskTowns plugin) {
        super(plugin, "Vault");
    }

    @Override
    public void onEnable() throws IllegalStateException {
        final RegisteredServiceProvider<Economy> economyProvider = ((BukkitHuskTowns) plugin).getServer()
                .getServicesManager().getRegistration(Economy.class);
        if (economyProvider == null) {
            throw new IllegalStateException("Could not resolve Vault economy provider");
        }
        this.economy = economyProvider.getProvider();
        plugin.log(Level.INFO, "Enabled Vault economy hook");
    }

    @Override
    public boolean takeMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount, @Nullable String reason) {
        return economy.withdrawPlayer(((BukkitUser) user).getPlayer(), amount.doubleValue()).transactionSuccess();
    }

    @Override
    public void giveMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount, @Nullable String reason) {
        economy.depositPlayer(((BukkitUser) user).getPlayer(), amount.doubleValue());
    }

    @Override
    public String formatMoney(@NotNull BigDecimal amount) {
        return economy.format(amount.doubleValue());
    }

}
