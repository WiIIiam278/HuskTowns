package net.william278.husktowns.listener;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Privilege;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EventListener {

    protected final HuskTowns plugin;

    public EventListener(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
    }

    protected boolean cancelOperation(@NotNull Operation operation) {
        final Optional<TownClaim> claim = plugin.getClaimAt(operation.getPosition());
        if (claim.isPresent()) {
            return cancelOperation(operation, claim.get());
        }
        final Optional<ClaimWorld> world = plugin.getClaimWorld(operation.getPosition().getWorld());
        if (world.isEmpty()) {
            return plugin.getDefaultRules().getUnclaimableWorldRules().isOperationAllowed(operation.getType());
        }
        return plugin.getDefaultRules().getWildernessRules().isOperationAllowed(operation.getType());
    }

    private boolean cancelOperation(@NotNull Operation operation, @NotNull TownClaim claim) {
        final Optional<OnlineUser> user = operation.getUser();
        final Town town = claim.town();
        boolean allowed = false;
        if (user.isPresent()) {
            final Optional<Member> member = plugin.getUserTown(user.get());
            if (member.isPresent() && member.get().town().equals(town)) {
                allowed = claim.claim().getType() == Claim.Type.PLOT && claim.claim().isPlotMember(user.get().getUuid())
                          || member.get().role().hasPrivilege(plugin, Privilege.TRUSTED_ACCESS);
            }
        }
        if (!allowed) {
            allowed = town.getRules().get(claim.claim().getType()).isOperationAllowed(operation.getType());
        }
        if (!allowed && !operation.isSilent()) {
            operation.getUser().ifPresent(onlineUser -> plugin.getLocales().getLocale("error_operation_not_allowed")
                    .ifPresent(onlineUser::sendMessage));
        }
        return allowed;
    }

    protected boolean cancelNature(@NotNull Chunk chunk1, @NotNull Chunk chunk2,
                                   @NotNull World world) {
        if (plugin.getClaimWorld(world).isEmpty()) {
            return false;
        }
        final Optional<TownClaim> claim1 = plugin.getClaimAt(chunk1, world);
        final Optional<TownClaim> claim2 = plugin.getClaimAt(chunk2, world);
        if (claim1.isPresent() && claim2.isPresent()) {
            return !claim1.get().town().equals(claim2.get().town());
        }
        return !(claim1.isEmpty() && claim2.isEmpty());
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
        //todo
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

    public void handleChunkChange(@NotNull OnlineUser user, @NotNull Position from, @NotNull Position to) {
        final Optional<TownClaim> fromClaim = plugin.getClaimAt(from);
        final Optional<TownClaim> toClaim = plugin.getClaimAt(to);
        if (fromClaim.map(TownClaim::town).equals(toClaim.map(TownClaim::town))) {
            return;
        }

        //todo
    }
}
