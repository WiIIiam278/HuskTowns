/*
 * This file is part of HuskTowns by William278. Do not redistribute!
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  All rights reserved.
 *
 *  This source code is provided as reference to licensed individuals that have purchased the HuskTowns
 *  plugin once from any of the official sources it is provided. The availability of this code does
 *  not grant you the rights to modify, re-distribute, compile or redistribute this source code or
 *  "plugin" outside this intended purpose. This license does not cover libraries developed by third
 *  parties that are utilised in the plugin.
 */

package net.william278.husktowns.util;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.config.Settings;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.SavedUser;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Class for carrying out data pruning operations
 */
public interface DataPruner {

    /**
     * Delete claims from claim worlds where the town no longer exists in the database
     */
    default void pruneOrphanClaims() {
        getPlugin().log(Level.INFO, "Validating and pruning orphan claims...");
        final LocalTime startTime = LocalTime.now();

        getPlugin().getClaimWorlds().values().forEach(world -> {
            world.getClaims().keySet().removeIf(
                    claim -> getPlugin().getTowns().stream().noneMatch(town -> town.getId() == claim)
            );
            getPlugin().getDatabase().updateClaimWorld(world);
        });

        getPlugin().log(Level.INFO, "Successfully validated and pruned orphan claims in " +
                                    (ChronoUnit.MILLIS.between(startTime, LocalTime.now()) / 1000d) + " seconds");
    }

    /**
     * Delete towns that have been inactive for a given number of days
     * <p>
     * This method will use the {@link Settings#getPruneInactiveTownDays()} setting to determine the number of days
     * a town must have been inactive for to be deleted. The {@link Settings#doAutomaticallyPruneInactiveTowns()} setting
     * must be enabled for this to work.
     */
    default void pruneInactiveTowns() {
        final int inactiveDays = getPlugin().getSettings().getPruneInactiveTownDays();
        if (!getPlugin().getSettings().doAutomaticallyPruneInactiveTowns() || inactiveDays <= 0) {
            return;
        }
        getPlugin().log(Level.INFO, "Pruning inactive towns...");
        final LocalTime startTime = LocalTime.now();

        final long pruned = this.pruneInactiveTowns(
                inactiveDays,
                getPlugin().getOnlineUsers().stream().findAny().orElse(null)
        );

        getPlugin().log(Level.INFO, "Successfully pruned " + pruned + " inactive towns in " +
                                    (ChronoUnit.MILLIS.between(startTime, LocalTime.now()) / 1000d) + " seconds");
    }

    /**
     * Delete towns that have been inactive for a given number of days
     *
     * @param daysInactive The number of days a town must have had no members login for to be deleted
     * @param actor        The user who is performing the deletion
     * @return The number of towns deleted
     */
    default long pruneInactiveTowns(long daysInactive, @Nullable OnlineUser actor) {
        // For cross-server propagation, an actor is required to perform the deletion
        if (actor == null && getPlugin().getSettings().doCrossServer()) {
            return 0L;
        }

        // Get inactive users
        final List<SavedUser> inactiveUsers = getPlugin().getDatabase().getInactiveUsers(daysInactive);
        final Set<UUID> inactiveUuids = inactiveUsers.stream()
                .map(SavedUser::user).map(User::getUuid)
                .collect(Collectors.toUnmodifiableSet());

        // Find towns matching the inactive users and prune
        return inactiveUsers.stream()
                .flatMap(user -> getPlugin().getUserTown(user.user()).stream().map(Member::town))
                .filter(town -> inactiveUuids.containsAll(town.getMembers().keySet())).distinct()
                .peek(town -> {
                    getPlugin().log(Level.INFO, "Pruning town " + town.getName() + "...");
                    getPlugin().getManager().towns().deleteTownData(actor, town);
                })
                .count();
    }

    @NotNull
    HuskTowns getPlugin();

}
