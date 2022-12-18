package net.william278.husktowns.town;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

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

        }

        public void deleteTown(@NotNull OnlineUser user) {

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
