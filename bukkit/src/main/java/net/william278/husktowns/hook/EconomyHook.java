package net.william278.husktowns.hook;

import net.william278.husktowns.HuskTowns;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyHook {
    private static Economy economy = null;
    private static final HuskTowns plugin = HuskTowns.getInstance();

    public static boolean initialize() {
        if (!HuskTowns.getSettings().doEconomy) {
            return false;
        }
        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
        if (vault == null) {
            plugin.getConfig().set("integrations.economy.enabled", false);
            plugin.saveConfig();
            return false;
        }
        if (!vault.isEnabled()) {
            return false;
        }
        RegisteredServiceProvider<Economy> economyProvider =
                plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        plugin.getLogger().info("Enabled Vault (economy) integration!");
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

    public static String format(double monetaryValue) {
        return economy.format(monetaryValue);
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
