package net.william278.husktowns.hook;

import com.djrapitops.plan.capability.CapabilityService;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.ExtensionService;
import com.djrapitops.plan.extension.annotation.*;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.town.Role;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.UUID;
import java.util.logging.Level;

public class PlanHook extends Hook {
    public PlanHook(@NotNull HuskTowns plugin) {
        super(plugin, "Plan");
    }

    @Override
    public void onEnable() {
        if (!areAllCapabilitiesAvailable()) {
            return;
        }
        registerDataExtension();
        listenForPlanReloads();
    }

    private boolean areAllCapabilitiesAvailable() {
        final CapabilityService capabilities = CapabilityService.getInstance();
        return capabilities.hasCapability("DATA_EXTENSION_VALUES");
    }

    private void registerDataExtension() {
        try {
            ExtensionService.getInstance().register(new PlanDataExtension(plugin));
        } catch (IllegalStateException | IllegalArgumentException e) {
            plugin.log(Level.WARNING, "Failed to register Plan data extension", e);
        }
    }


    // Register DataExtension again when Plan reloads
    private void listenForPlanReloads() {
        CapabilityService.getInstance().registerEnableListener(isPlanEnabled -> {
            if (isPlanEnabled) {
                registerDataExtension();
            }
        });
    }

    @PluginInfo(
            name = "HuskTowns",
            iconName = "city",
            iconFamily = Family.SOLID,
            color = Color.DEEP_PURPLE
    )
    @SuppressWarnings("unused")
    protected static class PlanDataExtension implements DataExtension {

        private HuskTowns plugin;

        private PlanDataExtension(@NotNull HuskTowns plugin) {
            this.plugin = plugin;
        }

        protected PlanDataExtension() {
        }

        @Override
        public CallEvents[] callExtensionMethodsOn() {
            return new CallEvents[]{
                    CallEvents.PLAYER_JOIN,
                    CallEvents.PLAYER_LEAVE,
                    CallEvents.SERVER_EXTENSION_REGISTER
            };
        }

        /*
         * User data providers
         */

        @BooleanProvider(
                text = "Is in Town",
                description = "Whether or not the player is a member of a town",
                priority = 100,
                iconFamily = Family.SOLID,
                conditionName = "townCondition",
                hidden = true
        )
        public boolean isPlayerInTown(@NotNull UUID uuid) {
            return plugin.getTowns().stream()
                    .map(town -> town.getMembers().containsKey(uuid))
                    .findFirst().orElse(false);
        }

        @StringProvider(
                text = "Town",
                description = "The town the player is a member of",
                priority = 99,
                iconName = "tag",
                iconFamily = Family.SOLID
        )
        @Conditional("townCondition")
        @NotNull
        public String playerTownName(@NotNull UUID uuid) {
            return plugin.getTowns().stream()
                    .filter(town -> town.getMembers().containsKey(uuid))
                    .map(Town::getName)
                    .findFirst().orElse("N/A");
        }

        @StringProvider(
                text = "Role",
                description = "The role the user has in the town",
                priority = 98,
                iconName = "id-badge",
                iconFamily = Family.SOLID
        )
        @Conditional("townCondition")
        @NotNull
        public String playerRoleName(@NotNull UUID uuid) {
            return plugin.getTowns().stream()
                    .filter(town -> town.getMembers().containsKey(uuid))
                    .map(town -> plugin.getRoles().fromWeight(town.getMembers().get(uuid)))
                    .map(role -> role.map(Role::getName).orElse("N/A"))
                    .findFirst().orElse("N/A");
        }

        /*
         * Server data providers
         */
        @NumberProvider(
                text = "Worlds",
                description = "Number of worlds where claims can be made on the server",
                priority = 100,
                iconName = "globe",
                iconFamily = Family.SOLID
        )
        public long claimWorlds() {
            return plugin.getClaimWorlds().size();
        }

        @NumberProvider(
                text = "Claims",
                description = "Total number of claims made on the server, including admin claims",
                priority = 99,
                iconName = "globe",
                iconFamily = Family.SOLID
        )
        public long totalClaims() {
            return plugin.getClaimWorlds().values().stream().mapToInt(ClaimWorld::getClaimCount).sum();
        }

        @NumberProvider(
                text = "Admin Claims",
                description = "Total number of admin claims made on the server",
                priority = 98,
                iconName = "gavel",
                iconFamily = Family.SOLID
        )
        public long totalAdminClaims() {
            return plugin.getClaimWorlds().values().stream().mapToInt(ClaimWorld::getAdminClaimCount).sum();
        }

        @NumberProvider(
                text = "Towns",
                description = "Total number of towns on the server",
                priority = 97,
                iconName = "city",
                iconFamily = Family.SOLID
        )
        public long totalTowns() {
            return plugin.getTowns().size();
        }

        @BooleanProvider(
                text = "Is economy enabled",
                description = "Whether or not an economy hook is available",
                priority = 96,
                iconFamily = Family.SOLID,
                conditionName = "economyCondition",
                hidden = true
        )
        public boolean economyEnabled() {
            return plugin.getEconomyHook().isPresent();
        }

        @StringProvider(
                text = "Total Wealth",
                description = "Total wealth of all towns on the server",
                priority = 95,
                iconFamily = Family.SOLID,
                iconName = "sack-dollar"
        )
        @Conditional("economyCondition")
        @NotNull
        public String totalWealth() {
            final BigDecimal wealth = plugin.getTowns().stream().map(Town::getMoney)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return plugin.getEconomyHook().map(hook -> hook.formatMoney(wealth))
                    .orElse(new DecimalFormat("#,###.##").format(wealth));
        }

        @StringProvider(
                text = "Average Wealth",
                description = "Average balance of all town coffers on the server",
                priority = 94,
                iconFamily = Family.SOLID,
                iconName = "money-bill"
        )
        @Conditional("economyCondition")
        @NotNull
        public String averageWealth() {
            final BigDecimal averageWealth = plugin.getTowns().stream()
                    .map(Town::getMoney)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(plugin.getTowns().size()), RoundingMode.FLOOR);
            return plugin.getEconomyHook().map(hook -> hook.formatMoney(averageWealth))
                    .orElse(new DecimalFormat("#,###.##").format(averageWealth));
        }

        @StringProvider(
                text = "Average Level",
                description = "Average level of all towns on the server",
                priority = 93,
                iconFamily = Family.SOLID,
                iconName = "star"
        )
        public String averageLevel() {
            final double averageLevel = plugin.getTowns().stream()
                    .mapToDouble(Town::getLevel)
                    .average()
                    .orElse(0);
            return new DecimalFormat("#.##").format(averageLevel);
        }
    }

}
