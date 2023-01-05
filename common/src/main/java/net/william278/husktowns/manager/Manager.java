package net.william278.husktowns.manager;

import net.kyori.adventure.text.Component;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.network.Message;
import net.william278.husktowns.network.Payload;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Privilege;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Manager {

    private final HuskTowns plugin;
    private final TownsManager towns;
    private final ClaimsManager claims;
    private final AdminManager admin;

    public Manager(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
        this.towns = new TownsManager(plugin);
        this.claims = new ClaimsManager(plugin);
        this.admin = new AdminManager(plugin);
    }

    @NotNull
    public TownsManager towns() {
        return towns;
    }

    @NotNull
    public ClaimsManager claims() {
        return claims;
    }

    @NotNull
    public AdminManager admin() {
        return admin;
    }

    public void updateTown(@NotNull OnlineUser user, @NotNull Town town) {
        plugin.getDatabase().updateTown(town);
        if (plugin.getTowns().contains(town)) {
            plugin.getTowns().replaceAll(t -> t.getId() == town.getId() ? town : t);
        } else {
            plugin.getTowns().add(town);
        }
        plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                .type(Message.Type.TOWN_UPDATE)
                .payload(Payload.integer(town.getId()))
                .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                .build()
                .send(broker, user));
    }

    protected Optional<Member> validateTownMembership(@NotNull OnlineUser user, @NotNull Privilege privilege) {
        final Optional<Member> member = plugin.getUserTown(user);
        if (member.isEmpty()) {
            plugin.getLocales().getLocale("error_not_in_town")
                    .ifPresent(user::sendMessage);
            return Optional.empty();
        }

        if (!member.get().hasPrivilege(plugin, privilege)) {
            plugin.getLocales().getLocale("error_insufficient_privileges", member.get().town().getName())
                    .ifPresent(user::sendMessage);
            return Optional.empty();
        }
        return member;
    }

    protected Optional<Member> validateTownMayor(@NotNull OnlineUser user) {
        final Optional<Member> member = plugin.getUserTown(user);
        if (member.isEmpty()) {
            plugin.getLocales().getLocale("error_not_in_town")
                    .ifPresent(user::sendMessage);
            return Optional.empty();
        }

        if (!member.get().role().equals(plugin.getRoles().getMayorRole())) {
            plugin.getLocales().getLocale("error_not_town_mayor", member.get().town().getName())
                    .ifPresent(user::sendMessage);
            return Optional.empty();
        }
        return member;
    }

    protected Optional<TownClaim> validateClaimOwnership(@NotNull Member member, @NotNull OnlineUser user,
                                                         @NotNull Chunk chunk, @NotNull World world) {
        final Optional<TownClaim> existingClaim = plugin.getClaimAt(chunk, world);
        if (existingClaim.isEmpty()) {
            plugin.getLocales().getLocale("error_chunk_not_claimed")
                    .ifPresent(user::sendMessage);
            return Optional.empty();
        }

        final TownClaim claim = existingClaim.get();
        final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
        if (claimWorld.isEmpty()) {
            plugin.getLocales().getLocale("error_world_not_claimable")
                    .ifPresent(user::sendMessage);
            return Optional.empty();
        }

        final Town town = member.town();
        if (!claim.town().equals(town)) {
            plugin.getLocales().getLocale("error_chunk_claimed_by", claim.town().getName())
                    .ifPresent(user::sendMessage);
            return Optional.empty();
        }
        return existingClaim;
    }

    public void sendTownNotification(@NotNull Town town, @NotNull Component message) {
        plugin.getOnlineUsers().stream()
                .filter(user -> town.getMembers().containsKey(user.getUuid()))
                .filter(user -> plugin.getUserPreferences(user.getUuid())
                        .map(Preferences::isTownNotifications).orElse(true))
                .forEach(user -> user.sendMessage(message));
    }

}
