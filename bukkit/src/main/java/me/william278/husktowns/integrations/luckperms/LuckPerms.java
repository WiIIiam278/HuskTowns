package me.william278.husktowns.integrations.luckperms;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.integrations.luckperms.calculators.ClaimCalculator;
import me.william278.husktowns.integrations.luckperms.calculators.PlayerAccessCalculator;
import me.william278.husktowns.integrations.luckperms.calculators.PlayerTownCalculator;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class LuckPerms {

    private ContextManager contextManager;
    private final List<ContextCalculator<Player>> registeredCalculators = new ArrayList<>();
    private static final HuskTowns plugin = HuskTowns.getInstance();

    public LuckPerms() {
        LuckPerms luckPerms = plugin.getServer().getServicesManager().load(LuckPerms.class);
        if (luckPerms == null) {
            plugin.getLogger().warning("Failed to load the LuckPermsAPI despite being installed; is LuckPerms up-to-date?");
            return;
        }
        this.contextManager = luckPerms.contextManager;
        registerProviders();
        plugin.getLogger().info("Enabled HuskTowns LuckPerms context provider hook!");
    }

    private void registerProviders() {
        registerProvider(ClaimCalculator::new);
        registerProvider(PlayerTownCalculator::new);
        registerProvider(PlayerAccessCalculator::new);
    }

    private void registerProvider(Supplier<ContextCalculator<Player>> calculatorSupplier) {
        ContextCalculator<Player> contextCalculator = calculatorSupplier.get();
        this.contextManager.registerCalculator(contextCalculator);
        this.registeredCalculators.add(contextCalculator);
    }

    public void unRegisterProviders() {
        this.registeredCalculators.forEach(contextCalculator -> this.contextManager.unregisterCalculator(contextCalculator));
        this.registeredCalculators.clear();
    }

}
