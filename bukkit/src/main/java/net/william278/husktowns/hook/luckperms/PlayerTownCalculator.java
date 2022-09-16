package net.william278.husktowns.hook.luckperms;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.cache.PlayerCache;
import net.william278.husktowns.town.TownRole;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class PlayerTownCalculator implements ContextCalculator<Player> {

    private static final String PLAYER_IS_TOWN_MEMBER = "husktowns:is-town-member";
    private static final String PLAYER_TOWN_NAME = "husktowns:town";
    private static final String PLAYER_TOWN_ROLE = "husktowns:town-role";

    @Override
    public void calculate(@NonNull Player target, @NonNull ContextConsumer consumer) {
        PlayerCache playerCache = HuskTowns.getPlayerCache();
        if (playerCache.hasLoaded()) {
            if (playerCache.isPlayerInTown(target.getUniqueId())) {
                consumer.accept(PLAYER_IS_TOWN_MEMBER, "true");
                consumer.accept(PLAYER_TOWN_NAME, playerCache.getPlayerTown(target.getUniqueId()));
                consumer.accept(PLAYER_TOWN_ROLE, playerCache.getPlayerRole(target.getUniqueId()).toString().toLowerCase());
            } else {
                consumer.accept(PLAYER_IS_TOWN_MEMBER, "false");
            }
        }
    }

    @Override
    @NotNull
    public ContextSet estimatePotentialContexts() {
        ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
        PlayerCache playerCache = HuskTowns.getPlayerCache();
        if (playerCache.hasLoaded()) {
            builder.add(PLAYER_IS_TOWN_MEMBER, "true");
            builder.add(PLAYER_IS_TOWN_MEMBER, "false");
            for (String town : playerCache.getTowns()) {
                builder.add(PLAYER_TOWN_NAME, town);
            }
            for (TownRole role : TownRole.townRoles) {
                builder.add(PLAYER_TOWN_ROLE, role.id().toLowerCase());
            }
        }
        return builder.build();
    }
}
