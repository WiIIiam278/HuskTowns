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

package net.william278.husktowns.util;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Action;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
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

import static net.william278.husktowns.config.Settings.TownSettings;

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

        getPlugin().getClaimWorlds().values().stream()
            .filter(world -> world.pruneOrphanClaims(getPlugin()))
            .forEach(w -> getPlugin().getDatabase().updateClaimWorld(w));

        getPlugin().log(Level.INFO, "Successfully validated and pruned orphan claims in " +
            (ChronoUnit.MILLIS.between(startTime, LocalTime.now()) / 1000d) + " seconds");
    }

    /**
     * Delete towns that have been inactive for a given number of days
     * <p>
     * This method will use the "prune_after_days" setting to determine the number of days a town must have
     * been inactive for to be deleted.
     * <p>
     * The "prune_on_startup" setting must be enabled for this to work.
     */
    default void pruneInactiveTowns() {
        final TownSettings.TownPruningSettings settings = getPlugin().getSettings().getTowns().getPruneInactiveTowns();
        final int inactiveDays = settings.getPruneAfterDays();
        if (!settings.isPruneOnStartup() || inactiveDays <= 0) {
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
        if (actor == null && getPlugin().getSettings().getCrossServer().isEnabled()) {
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

    /**
     * Removes expired local wars
     * <p>
     * This method will remove any local wars that have expired from the database and update the town data
     * accordingly.
     * <p>
     * The "wars.enabled" setting must be enabled for this to work.
     */
    default void pruneLocalTownWars() {
        final TownSettings.RelationsSettings settings = getPlugin().getSettings().getTowns().getRelations();
        if (!settings.isEnabled() || !settings.getWars().isEnabled()) {
            return;
        }

        final OnlineUser actor = getPlugin().getOnlineUsers().stream().findAny().orElse(null);
        final List<Town> warsToClear = getPlugin().getTowns().stream()
            .filter(town -> town.getCurrentWar().map(
                war -> !getPlugin().getSettings().getCrossServer().isEnabled() ||
                    war.getHostServer().equals(getPlugin().getServerName())
            ).orElse(false)).toList();
        warsToClear.forEach(town -> {
            town.clearCurrentWar();
            town.getLog().log(Action.of(Action.Type.LOST_WAR));
            if (actor != null) {
                getPlugin().getManager().updateTownData(actor, town);
            } else {
                getPlugin().getDatabase().updateTown(town);
                getPlugin().updateTown(town);
            }
        });
    }

    @NotNull
    HuskTowns getPlugin();

}
