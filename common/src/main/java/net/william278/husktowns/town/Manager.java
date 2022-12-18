package net.william278.husktowns.town;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.network.Message;
import net.william278.husktowns.network.Payload;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Manager {

    private final Towns towns;
    private final Claims claims;
    private final Admin admin;

    public Manager(@NotNull HuskTowns plugin) {
        this.towns = new Towns(plugin);
        this.claims = new Claims(plugin);
        this.admin = new Admin(plugin);
    }

    @NotNull
    public Towns towns() {
        return towns;
    }

    @NotNull
    public Claims claims() {
        return claims;
    }

    @NotNull
    public Admin admin() {
        return admin;
    }

    public static class Towns {
        private final HuskTowns plugin;

        private Towns(@NotNull HuskTowns plugin) {
            this.plugin = plugin;
        }

        public void createTown(@NotNull OnlineUser user, @NotNull String townName) {
            CompletableFuture.runAsync(() -> {
                // Check the user isn't already in a town
                if (plugin.getUserTown(user).isPresent()) {
                    plugin.getLocales().getLocale("error_already_in_town")
                            .ifPresent(user::sendMessage);
                    return;
                }

                // Check against invalid names and duplicates
                if (!plugin.getValidator().isValidTownName(townName)) {
                    plugin.getLocales().getLocale("error_invalid_town_name")
                            .ifPresent(user::sendMessage);
                    return;
                }

                final Town town = plugin.getDatabase().createTown(townName, user);
                plugin.getTowns().add(town);
                plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                        .type(Message.Type.TOWN_UPDATE)
                        .payload(Payload.integer(town.getId()))
                        .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                        .build()
                        .send(broker, user));

                plugin.getLocales().getLocale("town_created", town.getName())
                        .ifPresent(user::sendMessage);
            });
        }

        public void deleteTown(@NotNull OnlineUser user) {
            CompletableFuture.runAsync(() -> {
                final Optional<Member> member = plugin.getUserTown(user);
                if (member.isEmpty()) {
                    plugin.getLocales().getLocale("error_not_in_town")
                            .ifPresent(user::sendMessage);
                    return;
                }

                final Town town = member.get().town();
                if (!town.getMayor().equals(user.getUuid())) {
                    plugin.getLocales().getLocale("error_not_town_mayor", town.getName())
                            .ifPresent(user::sendMessage);
                    return;
                }

                plugin.getDatabase().deleteTown(town.getId());
                plugin.getTowns().remove(town);
                plugin.getClaimWorlds().values().forEach(world -> {
                    if (world.removeTownClaims(town.getId()) > 0) {
                        plugin.getDatabase().updateClaimWorld(world);
                    }
                });

                // Propagate the town deletion to all servers
                plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                        .type(Message.Type.TOWN_DELETE)
                        .payload(Payload.integer(town.getId()))
                        .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                        .build()
                        .send(broker, user));

                plugin.getLocales().getLocale("town_deleted", town.getName())
                        .ifPresent(user::sendMessage);
            });
        }

        public void inviteMember(@NotNull OnlineUser user, @NotNull String target) {

        }

        public void removeMember(@NotNull OnlineUser user, @NotNull String target) {

        }

        public void renameTown(@NotNull OnlineUser user, @NotNull String newName) {

        }

        public void setTownSpawn(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {

        }

        public void clearTownSpawn(@NotNull OnlineUser user) {

        }

        public void depositTownBank(@NotNull OnlineUser user, int amount) {

        }
    }

    public static class Claims {
        private final HuskTowns plugin;

        private Claims(@NotNull HuskTowns plugin) {
            this.plugin = plugin;
        }

        public void createClaim(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {

        }

        public void deleteClaim(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {

        }

        public void deleteAllClaims(@NotNull OnlineUser user) {

        }

        public void makeClaimPlot(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {

        }

        public void makeClaimFarm(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {

        }

        public void makeClaimRegular(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {

        }

        public void addPlotMember(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk, @NotNull String target) {

        }

        public void removePlotMember(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk, @NotNull String target) {

        }

    }

    public static class Admin {
        private final HuskTowns plugin;

        private Admin(@NotNull HuskTowns plugin) {
            this.plugin = plugin;
        }

    }
}
