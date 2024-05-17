/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.husktowns.hook;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.*;
import net.william278.cloplib.operation.OperationType;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.Rules;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Privilege;
import net.william278.husktowns.town.Role;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.BukkitUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;

public class LuckPermsHook extends Hook {
    private ContextManager contexts;
    private final List<ContextCalculator<Player>> calculators = new ArrayList<>();

    @PluginHook(id = "LuckPerms", register = PluginHook.Register.ON_ENABLE, platform = "bukkit")
    public LuckPermsHook(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        final RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider == null) {
            throw new IllegalStateException("Could not resolve LuckPerms provider");
        }
        final LuckPerms api = provider.getProvider();
        this.contexts = api.getContextManager();
        this.calculators.forEach(contextCalculator -> this.contexts.unregisterCalculator(contextCalculator));
        this.calculators.clear();
        this.registerCalculator(() -> new ClaimContextCalculator(plugin));
        this.registerCalculator(() -> new TownContextCalculator(plugin));
        plugin.log(Level.INFO, "Enabled LuckPerms context provider hook");
    }

    private void registerCalculator(final Supplier<ContextCalculator<Player>> calculatorSupplier) {
        final ContextCalculator<Player> contextCalculator = calculatorSupplier.get();
        this.contexts.registerCalculator(contextCalculator);
        this.calculators.add(contextCalculator);
    }

    private record ClaimContextCalculator(@NotNull HuskTowns plugin) implements ContextCalculator<Player> {
        @Override
        public void calculate(@NotNull Player target, @NotNull ContextConsumer consumer) {
            final Location location = target.getLocation();
            final Optional<TownClaim> claim = plugin.getClaimAt(Position.at(
                    location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                    World.of(target.getWorld().getUID(), target.getWorld().getName(),
                            target.getWorld().getEnvironment().name().toLowerCase())));
            if (claim.isEmpty()) {
                final Rules wilderness = plugin.getRulePresets().getWildernessRules(plugin.getFlags());
                setContextsFromRules(consumer, wilderness);
                consumer.accept(ContextKey.STANDING_IN_OWN_TOWN.getKey(plugin), "false");
                consumer.accept(ContextKey.IN_CLAIM_KEY.getKey(plugin), "false");
                return;
            }

            final TownClaim townClaim = claim.get();
            final Optional<Member> member = plugin.getUserTown(BukkitUser.adapt(target, plugin));
            if (member.isPresent() && member.get().town().equals(townClaim.town())) {
                final Member user = member.get();
                consumer.accept(ContextKey.STANDING_IN_OWN_TOWN.getKey(plugin), "true");
                if (user.hasPrivilege(plugin, Privilege.TRUSTED_ACCESS)) {
                    consumer.accept(ContextKey.CAN_PLAYER_BUILD.getKey(plugin), "true");
                    consumer.accept(ContextKey.CAN_PLAYER_OPEN_CONTAINERS.getKey(plugin), "true");
                    consumer.accept(ContextKey.CAN_PLAYER_INTERACT.getKey(plugin), "true");
                    consumer.accept(ContextKey.IN_CLAIM_KEY.getKey(plugin), "true");
                    return;
                }
            } else {
                consumer.accept(ContextKey.STANDING_IN_OWN_TOWN.getKey(plugin), "false");
            }

            final Rules rules = townClaim.town().getRules().get(townClaim.claim().getType());
            setContextsFromRules(consumer, rules);
        }

        @Override
        @NotNull
        public ContextSet estimatePotentialContexts() {
            final String[] contexts = {
                    ContextKey.STANDING_IN_OWN_TOWN.getKey(plugin),
                    ContextKey.CAN_PLAYER_BUILD.getKey(plugin),
                    ContextKey.CAN_PLAYER_OPEN_CONTAINERS.getKey(plugin),
                    ContextKey.CAN_PLAYER_INTERACT.getKey(plugin),
                    ContextKey.IN_CLAIM_KEY.getKey(plugin),
            };
            final ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
            for (final String context : contexts) {
                builder.add(context, "true");
                builder.add(context, "false");
            }

            // Claim town key contexts
            final HashSet<Integer> claimedTowns = new HashSet<>();
            for (final World world : plugin.getWorlds()) {
                plugin.getClaimWorld(world).ifPresent(claimWorld -> claimedTowns.addAll(claimWorld.getClaims().keySet()));
            }
            for (int town : claimedTowns) {
                builder.add(ContextKey.CLAIM_TOWN_KEY.getKey(plugin), plugin.getTowns().stream()
                        .filter(t -> t.getId() == town).findFirst().map(Town::getName).orElse("unknown"));
            }
            builder.add(
                    ContextKey.CLAIM_TOWN_KEY.getKey(plugin),
                    plugin.getSettings().getTowns().getAdminTown().getName()
            );
            return builder.build();
        }

        private void setContextsFromRules(@NotNull ContextConsumer consumer, Rules wilderness) {
            consumer.accept(ContextKey.CAN_PLAYER_BUILD.getKey(plugin), wilderness
                    .cancelOperation(OperationType.BLOCK_BREAK, plugin.getFlags()) ? "false" : "true");
            consumer.accept(ContextKey.CAN_PLAYER_OPEN_CONTAINERS.getKey(plugin), wilderness
                    .cancelOperation(OperationType.CONTAINER_OPEN, plugin.getFlags()) ? "false" : "true");
            consumer.accept(ContextKey.CAN_PLAYER_INTERACT.getKey(plugin), wilderness
                    .cancelOperation(OperationType.BLOCK_INTERACT, plugin.getFlags()) ? "false" : "true");
        }
    }

    private record TownContextCalculator(@NotNull HuskTowns plugin) implements ContextCalculator<Player> {
        @Override
        public void calculate(@NotNull Player target, @NotNull ContextConsumer consumer) {
            final Optional<Member> userTown = plugin.getUserTown(BukkitUser.adapt(target, plugin));
            if (userTown.isEmpty()) {
                consumer.accept(ContextKey.PLAYER_IS_TOWN_MEMBER.getKey(plugin), "false");
                return;
            }
            final Member member = userTown.get();
            consumer.accept(ContextKey.PLAYER_IS_TOWN_MEMBER.getKey(plugin), "true");
            consumer.accept(ContextKey.PLAYER_TOWN_NAME.getKey(plugin), member.town().getName());
            consumer.accept(ContextKey.PLAYER_TOWN_ROLE.getKey(plugin), member.role().getName());
            consumer.accept(ContextKey.PLAYER_TOWN_LEVEL.getKey(plugin), Integer.toString(member.town().getLevel()));
        }

        @Override
        @NotNull
        public ContextSet estimatePotentialContexts() {
            final ImmutableContextSet.Builder builder = ImmutableContextSet.builder()
                    .add(ContextKey.PLAYER_IS_TOWN_MEMBER.getKey(plugin), "true")
                    .add(ContextKey.PLAYER_IS_TOWN_MEMBER.getKey(plugin), "false");
            for (final String townName : plugin.getTowns().stream().map(Town::getName).toList()) {
                builder.add(ContextKey.PLAYER_TOWN_NAME.getKey(plugin), townName);
            }
            for (final String roleName : plugin.getRoles().getRoles().stream().map(Role::getName).toList()) {
                builder.add(ContextKey.PLAYER_TOWN_ROLE.getKey(plugin), roleName);
            }
            for (int i = 1; i <= plugin.getLevels().getMaxLevel(); i++) {
                builder.add(ContextKey.PLAYER_TOWN_LEVEL.getKey(plugin), Integer.toString(i));
            }
            return builder.build();
        }
    }

    public enum ContextKey {
        PLAYER_IS_TOWN_MEMBER("is-town-member"),
        PLAYER_TOWN_NAME("town"),
        PLAYER_TOWN_ROLE("town-role"),
        PLAYER_TOWN_LEVEL("town-level"),
        CAN_PLAYER_BUILD("can-build"),
        CAN_PLAYER_OPEN_CONTAINERS("can-open-containers"),
        CAN_PLAYER_INTERACT("can-interact"),
        STANDING_IN_OWN_TOWN("standing-in-own-town"),
        CLAIM_TOWN_KEY("claim-town"),
        IN_CLAIM_KEY("in-claim");

        private final String key;

        ContextKey(@NotNull String key) {
            this.key = key;
        }

        @NotNull
        public String getKey(@NotNull HuskTowns plugin) {
            return plugin.getKey(key).toString();
        }

    }

}
