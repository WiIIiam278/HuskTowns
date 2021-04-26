package me.william278.husktowns.integration;

import me.william278.husktowns.HuskTowns;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Vault {
    private static Economy economy = null;
    private static final HuskTowns plugin = HuskTowns.getInstance();

    public static boolean initializeEconomy() {
        RegisteredServiceProvider<Economy> economyProvider =
                plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

    public static String format(double monetaryValue) {
        return economy.format(monetaryValue);
    }

    public static boolean hasMoney(Player p, Double amount) {
        Economy e = economy;
        return e.getBalance(p) >= amount;
    }

    public static boolean takeMoney(Player p, Double amount) {
        Economy e = economy;
        if (e.getBalance(p) >= amount) {
            e.withdrawPlayer(p, amount);
            return true;
        } else {
            return false;
        }
    }
}
