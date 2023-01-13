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
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Manager, for interfacing and editing town, claim and user data
 */
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

    public void updateTownData(@NotNull OnlineUser actor, @NotNull Town town) {
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
                .send(broker, actor));
    }

    public void editTown(@NotNull OnlineUser user, @NotNull Consumer<Member> editor, @Nullable Consumer<Member> thenRun,
                         @NotNull Privilege... privileges) {
        ifPrivilegedMember(user, privileges).ifPresent(member -> plugin.runAsync(() -> {
            editor.accept(member);
            updateTownData(user, member.town());
            if (thenRun != null) {
                thenRun.accept(member);
            }
        }));
    }

    public void editTown(@NotNull OnlineUser user, @NotNull Consumer<Member> editor, @NotNull Privilege... privileges) {
        editTown(user, editor, null, privileges);
    }

    public void editTownAsMayor(@NotNull OnlineUser user, @NotNull Consumer<Member> editor, @Nullable Consumer<Member> thenRun) {
        ifMayor(user).ifPresent(member -> plugin.runAsync(() -> {
            editor.accept(member);
            updateTownData(user, member.town());
            if (thenRun != null) {
                thenRun.accept(member);
            }
        }));
    }

    protected Optional<Member> ifPrivilegedMember(@NotNull OnlineUser user, @NotNull Privilege... privileges) {
        final Optional<Member> member = plugin.getUserTown(user);
        if (member.isEmpty()) {
            plugin.getLocales().getLocale("error_not_in_town")
                    .ifPresent(user::sendMessage);
            return Optional.empty();
        }

        for (Privilege privilege : privileges) {
            if (!member.get().hasPrivilege(plugin, privilege)) {
                plugin.getLocales().getLocale("error_insufficient_privileges", member.get().town().getName())
                        .ifPresent(user::sendMessage);
                return Optional.empty();
            }
        }
        return member;
    }

    protected Optional<Member> ifMayor(@NotNull OnlineUser user) {
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

    protected Optional<TownClaim> ifClaimOwner(@NotNull Member member, @NotNull OnlineUser user,
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
