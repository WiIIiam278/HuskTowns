package net.william278.husktowns.listener;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.network.Broker;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
import net.william278.husktowns.user.SavedUser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EventListener {

    protected final HuskTowns plugin;

    public EventListener(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
    }

    @NotNull
    protected OperationHandler handler() {
        return plugin.getOperationHandler();
    }

    protected void onPlayerJoin(@NotNull OnlineUser user) {
        plugin.runAsync(() -> {
            final Optional<SavedUser> userData = plugin.getDatabase().getUser(user.getUuid());
            if (userData.isEmpty()) {
                plugin.getDatabase().createUser(user, Preferences.getDefaults());
                plugin.setUserPreferences(user.getUuid(), Preferences.getDefaults());
                return;
            }

            // Update the user's name if it has changed
            final SavedUser savedUser = userData.get();
            final Preferences preferences = savedUser.preferences();
            boolean updateNeeded = false;

            if (preferences.isTownChatTalking()) {
                if (plugin.getUserTown(user).isEmpty()) {
                    preferences.setTownChatTalking(false);
                    updateNeeded = true;
                } else {
                    plugin.getLocales().getLocale("town_chat_reminder")
                            .ifPresent(user::sendMessage);
                }
            }

            if (!savedUser.user().getUsername().equals(user.getUsername())) {
                updateNeeded = true;
            }

            if (plugin.getSettings().doCrossServer()) {
                if (plugin.getSettings().getBrokerType() == Broker.Type.PLUGIN_MESSAGE && plugin.getOnlineUsers().size() == 1) {
                    plugin.setLoaded(false);
                    plugin.loadData();
                }

                if (preferences.getTeleportTarget().isPresent()) {
                    final Position position = preferences.getTeleportTarget().get();
                    plugin.runSync(() -> {
                        user.teleportTo(position);
                        plugin.getLocales().getLocale("teleportation_complete")
                                .ifPresent(user::sendActionBar);
                    });

                    preferences.clearTeleportTarget();
                    updateNeeded = true;
                }
            }

            plugin.setUserPreferences(user.getUuid(), preferences);
            if (updateNeeded) {
                plugin.getDatabase().updateUser(user, preferences);
            }
        });
    }

    protected void onPlayerInspect(@NotNull OnlineUser user, @NotNull Position position) {
        final Optional<TownClaim> claim = plugin.getClaimAt(position);
        if (claim.isPresent()) {
            final TownClaim townClaim = claim.get();
            final Claim claimData = townClaim.claim();
            plugin.highlightClaim(user, townClaim);
            if (townClaim.isAdminClaim(plugin)) {
                plugin.getLocales().getLocale("inspect_chunk_admin_claim",
                                Integer.toString(claimData.getChunk().getX()), Integer.toString(claimData.getChunk().getZ()))
                        .ifPresent(user::sendMessage);
                return;
            }
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

    public boolean handlePlayerChat(@NotNull OnlineUser user, @NotNull String message) {
        final Optional<Preferences> preferences = plugin.getUserPreferences(user.getUuid());
        if (preferences.isPresent() && preferences.get().isTownChatTalking()) {
            plugin.getManager().towns().sendChatMessage(user, message);
            return true;
        }
        return false;
    }
}
