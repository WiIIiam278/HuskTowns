package net.william278.husktowns.listener;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class EventListener {

    protected final HuskTowns plugin;

    public EventListener(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
    }

    protected void onPlayerJoin(@NotNull OnlineUser user) {
        plugin.runAsync(() -> {
            final Optional<User> userData = plugin.getDatabase().getUser(user.getUuid());
            if (userData.isEmpty()) {
                plugin.getDatabase().createUser(user);
                return;
            }

            // Update the user's name if it has changed
            if (!userData.get().getUsername().equals(user.getUsername())) {
                plugin.getDatabase().updateUser(user);
            }
        });
    }

    protected void onPlayerQuit(@NotNull OnlineUser user) {
    }

    protected void onPlayerInspect(@NotNull OnlineUser user, @NotNull Position position) {
        final Optional<TownClaim> claim = plugin.getClaimAt(position);
        if (claim.isPresent()) {
            final TownClaim townClaim = claim.get();
            final Claim claimData = townClaim.claim();
            plugin.highlightClaim(user, townClaim);
            plugin.getLocales().getLocale("inspect_chunk_claimed_" + claimData.getType().name().toLowerCase(),
                            Integer.toString(claimData.getChunk().getX()), Integer.toString(claimData.getChunk().getZ()),
                            townClaim.town().getName())
                    .ifPresent(user::sendMessage);
            return;
        }
        if (plugin.getClaimWorld(user.getWorld()).isEmpty()) {
            plugin.getLocales().getLocale("inspect_chunk_not_claimable")
                    .ifPresent(user::sendMessage);
            return;
        }
        plugin.getLocales().getLocale("inspect_chunk_not_claimed")
                .ifPresent(user::sendMessage);
    }
}
