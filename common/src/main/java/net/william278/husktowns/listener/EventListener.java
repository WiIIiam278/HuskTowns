package net.william278.husktowns.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.network.Broker;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Privilege;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
import net.william278.husktowns.user.SavedUser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EventListener {

    private static final String ADMIN_CLAIM_ACCESS_PERMISSION = "husktowns.admin_claim_access";
    protected final HuskTowns plugin;

    public EventListener(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
    }

    protected boolean cancelOperation(@NotNull Operation operation) {
        if (!plugin.isLoaded()) {
            operation.getUser().ifPresent(onlineUser -> plugin.getLocales().getLocale("error_not_loaded")
                    .ifPresent(onlineUser::sendMessage));
            return true;
        }
        final Optional<TownClaim> claim = plugin.getClaimAt(operation.getPosition());
        if (claim.isPresent()) {
            return cancelOperation(operation, claim.get());
        }
        final Optional<ClaimWorld> world = plugin.getClaimWorld(operation.getPosition().getWorld());
        if (world.isEmpty() && plugin.getRulePresets().getUnclaimableWorldRules().cancelOperation(operation.getType())) {
            if (!operation.isSilent() && operation.getUser().isPresent()) {
                plugin.getLocales().getLocale("operation_cancelled")
                        .ifPresent(operation.getUser().get()::sendMessage);
            }
            return true;
        }
        if (plugin.getRulePresets().getWildernessRules().cancelOperation(operation.getType())) {
            if (!operation.isSilent() && operation.getUser().isPresent()) {
                plugin.getLocales().getLocale("operation_cancelled")
                        .ifPresent(operation.getUser().get()::sendMessage);
            }
            return true;
        }
        return false;
    }

    private boolean cancelOperation(@NotNull Operation operation, @NotNull TownClaim townClaim) {
        final Optional<OnlineUser> optionalUser = operation.getUser();
        final Town town = townClaim.town();
        final Claim claim = townClaim.claim();

        // If the operation is not allowed by the claim flags
        if (town.getRules().get(claim.getType()).cancelOperation(operation.getType())) {
            if (optionalUser.isEmpty()) {
                return true;
            }

            // Handle admin claims
            final OnlineUser user = optionalUser.get();
            if (townClaim.isAdminClaim(plugin) && user.hasPermission(ADMIN_CLAIM_ACCESS_PERMISSION)) {
                return false;
            }

            // Handle plot memberships
            final Claim.Type claimType = claim.getType();
            if (claimType == Claim.Type.PLOT && claim.isPlotMember(user.getUuid())) {
                return false;
            }

            // Handle ignoring claims
            if (plugin.getUserPreferences(user.getUuid()).map(Preferences::isIgnoringClaims).orElse(false)) {
                return false;
            }

            final Optional<Member> optionalMember = plugin.getUserTown(user);
            if (optionalMember.isEmpty()) {
                if (!operation.isSilent()) {
                    plugin.getLocales().getLocale("operation_cancelled_claimed",
                            town.getName()).ifPresent(user::sendMessage);
                }
                return true;
            }

            final Member member = optionalMember.get();
            if (!member.town().equals(town)) {
                if (!operation.isSilent()) {
                    plugin.getLocales().getLocale("operation_cancelled_claimed",
                            town.getName()).ifPresent(user::sendMessage);
                }
                return true;
            }

            if (!member.hasPrivilege(plugin, Privilege.TRUSTED_ACCESS)) {
                if (!operation.isSilent()) {
                    plugin.getLocales().getLocale("operation_cancelled_privileges")
                            .ifPresent(user::sendMessage);
                }
                return true;
            }
            return false;
        }
        return false;
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
            final Optional<SavedUser> userData = plugin.getDatabase().getUser(user.getUuid());
            if (userData.isEmpty()) {
                plugin.getDatabase().createUser(user, Preferences.getDefaults());
                plugin.setUserPreferences(user.getUuid(), Preferences.getDefaults());
                return;
            }

            // Update the user's name if it has changed
            final SavedUser savedUser = userData.get();
            final Preferences preferences = savedUser.preferences();
            plugin.setUserPreferences(user.getUuid(), preferences);
            if (!savedUser.user().getUsername().equals(user.getUsername())) {
                plugin.getDatabase().updateUser(user, preferences);
                if (preferences.isTownChatTalking()) {
                    plugin.getLocales().getLocale("town_chat_reminder")
                            .ifPresent(user::sendMessage);
                }
            }

            if (plugin.getSettings().crossServer) {
                if (plugin.getSettings().brokerType == Broker.Type.PLUGIN_MESSAGE
                    && plugin.getOnlineUsers().size() == 1) {
                    plugin.setLoaded(false);
                    plugin.loadData();
                }

                if (preferences.getCurrentTeleportTarget().isPresent()) {
                    plugin.runSync(() -> {
                        user.teleportTo(preferences.getCurrentTeleportTarget().get());
                        plugin.getLocales().getLocale("teleportation_complete")
                                .ifPresent(user::sendMessage);
                        preferences.clearCurrentTeleportTarget();
                    });
                }
            }
        });
    }

    protected void onPlayerQuit(@NotNull OnlineUser user) {
        plugin.runAsync(() -> plugin.getUserPreferences(user.getUuid())
                .ifPresent(preferences -> plugin.getDatabase().updateUser(user, preferences)));
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

        // Auto-claiming
        if (toClaim.isEmpty() && plugin.getUserPreferences(user.getUuid())
                .map(Preferences::isAutoClaimingLand)
                .orElse(false)) {
            plugin.getManager().claims().createClaim(user, to.getWorld(), to.getChunk(), false);
            return;
        }
        if (fromClaim.map(TownClaim::town).equals(toClaim.map(TownClaim::town))) {
            return;
        }

        // Claim entry/exit messages
        toClaim.ifPresentOrElse(entering -> {
            final Town town = entering.town();
            final TextColor color = TextColor.fromHexString(town.getColorRgb());
            user.sendActionBar(Component.text(town.getName()).color(color));
            if (town.getGreeting().isPresent()) {
                user.sendMessage(Component.text(town.getGreeting().get()).color(color));
            } else {
                plugin.getLocales().getLocale("entering_town", town.getName(), town.getColorRgb())
                        .ifPresent(user::sendMessage);
            }
        }, () -> {
            if (fromClaim.isPresent()) {
                final Town town = fromClaim.get().town();
                plugin.getLocales().getLocale("wilderness").ifPresent(user::sendActionBar);
                if (town.getFarewell().isPresent()) {
                    user.sendMessage(Component.text(town.getFarewell().get()).color(TextColor.fromHexString(town.getColorRgb())));
                } else {
                    plugin.getLocales().getLocale("leaving_town", town.getName(), town.getColorRgb())
                            .ifPresent(user::sendMessage);
                }
            }
        });
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
