package net.william278.husktowns.war;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Spawn;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * A declaration of war between two towns
 *
 * @param attackingTown the attacking town (sending the declaration)
 * @param defendingTown the defending town
 * @param sender        the sender of the declaration
 * @param expiry        the time the declaration expires
 * @since 2.6
 */
public record Declaration(
        @Expose @SerializedName("attacking_town") int attackingTown,
        @Expose @SerializedName("defending_town") int defendingTown,
        @Expose BigDecimal wager,
        @Expose User sender,
        @Expose OffsetDateTime expiry
) {
    @NotNull
    public static Declaration create(@NotNull Member sender, @NotNull Town defendingTown, @NotNull BigDecimal wager) {
        return new Declaration(
                sender.town().getId(),
                defendingTown.getId(),
                wager,
                sender.user(),
                OffsetDateTime.now().plusMinutes(10)
        );
    }

    public boolean hasExpired() {
        return OffsetDateTime.now().isAfter(expiry);
    }

    @NotNull
    public Optional<String> getWarServerName(@NotNull HuskTowns plugin) {
        return getDefendingTown(plugin).flatMap(Town::getSpawn).map(Spawn::getServer);
    }

    public Optional<Town> getAttackingTown(@NotNull HuskTowns plugin) {
        return plugin.findTown(attackingTown);
    }

    public Optional<Town> getDefendingTown(@NotNull HuskTowns plugin) {
        return plugin.findTown(defendingTown);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Declaration declaration) {
            return declaration.attackingTown == attackingTown && declaration.defendingTown == defendingTown;
        }
        return false;
    }

}
