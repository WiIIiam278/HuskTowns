package net.william278.husktowns.manager;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Action;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.events.IMemberJoinEvent;
import net.william278.husktowns.map.ClaimMap;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Role;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class AdminManager {
    private final HuskTowns plugin;

    protected AdminManager(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
    }

    private Optional<Town> getTownByName(@NotNull String townName) {
        return plugin.getTowns().stream()
                .filter(town -> town.getName().equalsIgnoreCase(townName))
                .findFirst();
    }

    public void createAdminClaim(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk, boolean showMap) {
        final Optional<TownClaim> existingClaim = plugin.getClaimAt(chunk, world);
        if (existingClaim.isPresent()) {
            plugin.getLocales().getLocale("error_chunk_claimed_by", existingClaim.get().town().getName())
                    .ifPresent(user::sendMessage);
            return;
        }

        final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
        if (claimWorld.isEmpty()) {
            plugin.getLocales().getLocale("error_world_not_claimable")
                    .ifPresent(user::sendMessage);
            return;
        }

        plugin.runAsync(() -> {
            final TownClaim claim = TownClaim.admin(chunk, plugin);
            plugin.getManager().claims().createClaimData(user, claim, world);
            plugin.getLocales().getLocale("admin_claim_created",
                            Integer.toString(chunk.getX()), Integer.toString(chunk.getZ()))
                    .ifPresent(user::sendMessage);
            plugin.highlightClaim(user, claim);
            if (showMap) {
                user.sendMessage(ClaimMap.builder(plugin)
                        .center(user.getChunk()).world(user.getWorld())
                        .build()
                        .toComponent(user));
            }
        });
    }


    public void deleteClaim(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk, boolean showMap) {
        final Optional<TownClaim> existingClaim = plugin.getClaimAt(chunk, world);
        if (existingClaim.isEmpty()) {
            plugin.getLocales().getLocale("error_chunk_not_claimed")
                    .ifPresent(user::sendMessage);
            return;
        }

        final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
        assert claimWorld.isPresent();

        plugin.runAsync(() -> {
            plugin.getManager().claims().deleteClaimData(user, existingClaim.get(), world);
            plugin.getLocales().getLocale("claim_deleted", Integer.toString(chunk.getX()),
                    Integer.toString(chunk.getZ())).ifPresent(user::sendMessage);
            if (showMap) {
                user.sendMessage(ClaimMap.builder(plugin)
                        .center(user.getChunk()).world(user.getWorld())
                        .build()
                        .toComponent(user));
            }
        });
    }

    public void deleteTown(@NotNull OnlineUser user, @NotNull String townName) {
        getTownByName(townName).ifPresentOrElse(town -> plugin.getManager().towns().deleteTown(user, town),
                () -> plugin.getLocales().getLocale("error_town_not_found", townName)
                        .ifPresent(user::sendMessage));
    }

    public void takeOverTown(@NotNull OnlineUser user, @NotNull String townName) {
        getTownByName(townName).ifPresentOrElse(namedTown -> {
            final Optional<Member> existingMembership = plugin.getUserTown(user);
            if (existingMembership.isPresent()) {
                plugin.getLocales().getLocale("error_already_in_town")
                        .ifPresent(user::sendMessage);
                return;
            }

            final Role mayorRole = plugin.getRoles().getMayorRole();
            plugin.fireEvent(plugin.getMemberJoinEvent(user, namedTown, mayorRole, IMemberJoinEvent.JoinReason.ADMIN_TAKE_OVER),
                    (event -> plugin.getManager().editTown(user, namedTown, (town -> {
                        town.getMembers().put(town.getMayor(), plugin.getRoles().getDefaultRole().getWeight());
                        town.getMembers().put(user.getUuid(), mayorRole.getWeight());
                        town.getLog().log(Action.of(user, Action.Type.ADMIN_TAKE_OVER, user.getUsername()));
                        plugin.getLocales().getLocale("town_assumed_ownership", town.getName())
                                .ifPresent(user::sendMessage);
                    }))));
        }, () -> plugin.getLocales().getLocale("error_town_not_found", townName)
                .ifPresent(user::sendMessage));
    }

    public void setTownBonus(@NotNull CommandUser user, @NotNull String townName, @NotNull Town.Bonus bonus, int amount) {
        final int value = Math.max(amount, 0);
        final boolean clearing = value == 0;
        final Optional<Town> optionalTown = getTownByName(townName);
        if (optionalTown.isEmpty()) {
            plugin.getLocales().getLocale("error_town_not_found", townName)
                    .ifPresent(user::sendMessage);
            return;
        }

        final OnlineUser onlineUser = user instanceof OnlineUser ? (OnlineUser) user : plugin.getOnlineUsers().stream()
                .findFirst()
                .orElse(null);
        if (onlineUser == null) {
            plugin.getLocales().getLocale("error_no_online_players")
                    .ifPresent(user::sendMessage);
            return;
        }
        plugin.runAsync(() -> plugin.getManager().editTown(onlineUser, optionalTown.get(), (town -> {
            final String bonusLog = bonus.name().toLowerCase() + ": " + value;
            final Action.Type action = !clearing ? Action.Type.ADMIN_SET_BONUS : Action.Type.ADMIN_CLEAR_BONUS;
            if (user instanceof OnlineUser) {
                town.getLog().log(Action.of(onlineUser, action, bonusLog));
            } else {
                town.getLog().log(Action.of(action, bonusLog));
            }
            town.setBonus(bonus, value);

            if (!clearing) {
                plugin.getLocales().getLocale("town_bonus_set", bonus.name().toLowerCase(), town.getName(),
                                Integer.toString(value))
                        .ifPresent(user::sendMessage);
            } else {
                plugin.getLocales().getLocale("town_bonus_cleared", bonus.name().toLowerCase(), town.getName())
                        .ifPresent(user::sendMessage);
            }
        })));
    }

    public void addTownBonus(@NotNull CommandUser user, @NotNull String townName, @NotNull Town.Bonus bonus, int amount) {
        final Optional<Town> town = getTownByName(townName);
        if (town.isEmpty()) {
            plugin.getLocales().getLocale("error_town_not_found", townName)
                    .ifPresent(user::sendMessage);
            return;
        }
        setTownBonus(user, townName, bonus, town.get().getBonus(bonus) + amount);
    }

    public void removeTownBonus(@NotNull CommandUser user, @NotNull String townName, @NotNull Town.Bonus bonus, int amount) {
        addTownBonus(user, townName, bonus, -amount);
    }

    public void clearTownBonus(@NotNull CommandUser user, @NotNull String townName, @NotNull Town.Bonus bonus) {
        setTownBonus(user, townName, bonus, 0);
    }

    public void viewTownBonus(@NotNull CommandUser user, @NotNull String townName) {
        final Optional<Town> optionalTown = getTownByName(townName);
        if (optionalTown.isEmpty()) {
            plugin.getLocales().getLocale("error_town_not_found", townName)
                    .ifPresent(user::sendMessage);
            return;
        }
        final Town town = optionalTown.get();

        // Send the town bonus entries for the town
        Component component = Component.empty();
        int skipped = 0;
        for (final Town.Bonus bonus : Town.Bonus.values()) {
            final int value = town.getBonus(bonus);
            if (value == 0) {
                skipped++;
                continue;
            }
            component = component.append(plugin.getLocales().getLocale("town_bonus_entry",
                            bonus.name().toLowerCase(), Integer.toString(value))
                    .map(MineDown::toComponent)
                    .map(com -> com.append(Component.newline()))
                    .orElse(Component.empty()));
        }

        // If no entries were displayed
        if (skipped >= Town.Bonus.values().length) {
            plugin.getLocales().getLocale("error_no_town_bonuses", town.getName())
                    .ifPresent(user::sendMessage);
            return;
        }

        plugin.getLocales().getLocale("town_bonus_list", town.getName())
                .map(MineDown::toComponent)
                .ifPresent(user::sendMessage);
        user.sendMessage(component);
    }

    public void sendLocalSpyMessage(@NotNull Town town, @NotNull Member sender, @NotNull String text) {
        plugin.getOnlineUsers().stream()
                .filter(user -> !town.getMembers().containsKey(user.getUuid()))
                .filter(user -> plugin.getUserPreferences(user.getUuid()).orElse(Preferences.getDefaults()).isTownChatSpying())
                .forEach(user -> plugin.getLocales().getLocale("town_chat_spy_message_format",
                                town.getName(), sender.user().getUsername(), sender.role().getName(), text)
                        .ifPresent(user::sendMessage));
    }
}
