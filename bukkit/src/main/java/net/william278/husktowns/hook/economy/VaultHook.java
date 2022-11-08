package net.william278.husktowns.hook.economy;

import net.milkbowl.vault.economy.Economy;
import net.william278.husktowns.HuskTowns;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {
    protected Economy economy;
    protected final HuskTowns plugin;

    public VaultHook(HuskTowns plugin) {
        this.plugin = plugin;
    }

    public boolean initialize() {
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

    public String format(double monetaryValue) {
        return economy.format(monetaryValue);
    }

    public boolean takeMoney(Player p, Double amount) {
        if (economy.getBalance(p) >= amount) {
            economy.withdrawPlayer(p, amount);
            return true;
        }
        return false;
    }
}
