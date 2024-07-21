/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.husktowns.migrator;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Log;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.database.Database;
import net.william278.husktowns.town.Spawn;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.Preferences;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.time.ZoneOffset;
import java.util.*;
import java.util.logging.Level;

/**
 * Migrator for HuskTowns v1.x to v2.x data
 */
public class LegacyMigrator extends Migrator {
    public LegacyMigrator(@NotNull HuskTowns plugin) {
        super(plugin, "Legacy");
        Arrays.stream(Parameters.values()).forEach(flag -> setParameter(flag.name().toLowerCase(), flag.getDefault()));
    }

    @Override
    protected void onStart() {
        // Convert towns
        plugin.log(Level.INFO, "Migrating towns...");
        plugin.getTowns().clear();
        plugin.getDatabase().deleteAllTowns();
        getConvertedTowns().forEach(town -> {
            try {
                town.setId(plugin.getDatabase().createTown(town.getName(),
                    User.of(town.getMayor(), "(Migrated)")).getId());
                plugin.getDatabase().updateTown(town);
                plugin.getTowns().add(town);
            } catch (IllegalStateException e) {
                plugin.log(Level.WARNING, "Skipped migrating " + town.getName() + ": " + e.getMessage());
            }
        });

        // Convert claims into claim worlds
        plugin.log(Level.INFO, "Migrating claims...");
        plugin.getClaimWorlds().clear();
        getConvertedClaimWorlds().forEach((serverWorld, claimWorld) -> plugin.getDatabase().updateClaimWorld(claimWorld));
        plugin.pruneOrphanClaims();

        // Copy over username/uuid data to the new database
        plugin.log(Level.INFO, "Migrating user records (this may take some time)...");
        plugin.getDatabase().deleteAllUsers();
        getConvertedUsers().forEach(user -> plugin.getDatabase().createUser(user, Preferences.getDefaults()));
    }

    @NotNull
    protected List<Town> getConvertedTowns() {
        final List<Town> towns = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                formatStatement("SELECT * FROM %towns%"))) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        final BigDecimal balance = BigDecimal.valueOf(resultSet.getDouble("money"));
                        final String name = resultSet.getString("name");
                        final int level = plugin.getLevels().getHighestLevelFor(balance);
                        final Timestamp founded = resultSet.getTimestamp("founded");
                        towns.add(Town.builder()
                            .id(resultSet.getInt("id"))
                            .name(name)
                            .options(Town.Options.builder()
                                .bio(clearLegacyFormatting(
                                    resultSet.getString("bio")))
                                .greeting(clearLegacyFormatting(
                                    resultSet.getString("greeting_message")))
                                .farewell(clearLegacyFormatting(
                                    resultSet.getString("farewell_message")))
                                .color(Town.Options.getRandomTextColor(name).asHexString())
                                .build())
                            .log(Log.migratedLog(founded.toLocalDateTime().atOffset(ZoneOffset.UTC)))
                            .money(balance.subtract(plugin.getLevels().getTotalCostFor(level)))
                            .rules(plugin.getRulePresets().getDefaultRules().getDefaults(plugin.getFlags()))
                            .level(level)
                            .build());
                    }
                }
            }

            // Set the members for each town
            try (PreparedStatement statement = connection.prepareStatement(
                formatStatement("SELECT * FROM %players% WHERE `town_id` IS NOT NULL"))) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        final int townId = resultSet.getInt("town_id");
                        final int roleWeight = resultSet.getInt("town_role");
                        final Town town = towns.stream().filter(t -> t.getId() == townId).findFirst().orElseThrow();
                        town.addMember(UUID.fromString(resultSet.getString("uuid")),
                            plugin.getRoles().fromWeight(roleWeight)
                                .or(() -> {
                                    plugin.log(Level.WARNING, "No role found for weight: " + roleWeight + " - expect errors! " +
                                        "Have you updated your roles.yml to match your existing setup? If not, stop the server, " +
                                        "reset your database and start migration again.");
                                    return Optional.of(plugin.getRoles().getDefaultRole());
                                }).orElseThrow());
                    }
                }
            }

            // Select the number of claims for each town
            try (PreparedStatement statement = connection.prepareStatement(
                formatStatement("SELECT `town_id`, COUNT(*) AS `claims` FROM %claims% GROUP BY `town_id`"))) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        final int townId = resultSet.getInt("town_id");
                        final Town town = towns.stream().filter(t -> t.getId() == townId).findFirst().orElseThrow();
                        town.setClaimCount(resultSet.getInt("claims"));
                    }
                }
            }

            // Select the spawn for each town
            try (PreparedStatement statement = connection.prepareStatement(formatStatement("""
                SELECT %towns%.`id` AS `town_id`, `is_spawn_public`, `server`, `world`, `x`, `y`, `z`, `yaw`, `pitch` FROM %towns%
                INNER JOIN %locations%
                ON %towns%.`spawn_location_id` = %locations%.`id`"""))) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        final int townId = resultSet.getInt("town_id");
                        final Town town = towns.stream().filter(t -> t.getId() == townId).findFirst().orElseThrow();
                        final String worldName = resultSet.getString("world");
                        final Spawn spawn = Spawn.of(Position.at(
                                resultSet.getDouble("x"),
                                resultSet.getDouble("y"),
                                resultSet.getDouble("z"),
                                World.of(new UUID(0, 0),
                                    worldName, determineEnvironment(worldName)),
                                resultSet.getFloat("yaw"),
                                resultSet.getFloat("pitch")),
                            resultSet.getString("server"));
                        spawn.setPublic(resultSet.getBoolean("is_spawn_public"));
                        town.setSpawn(spawn);
                    }
                }
            }

            // Select the total bonus_claims and bonus_members for each town based on ID from the %bonuses% table
            try (PreparedStatement statement = connection.prepareStatement(formatStatement("""
                SELECT `town_id`, SUM(`bonus_claims`) AS `bonus_claims`, SUM(`bonus_members`) AS `bonus_members`
                FROM %bonuses% GROUP BY `town_id`"""))) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        final int townId = resultSet.getInt("town_id");
                        final Town town = towns.stream().filter(t -> t.getId() == townId).findFirst().orElseThrow();
                        town.setBonusClaims(resultSet.getInt("bonus_claims"));
                        town.setBonusMembers(resultSet.getInt("bonus_members"));
                    }
                }
            }

            // Set the flag settings for each town
            try (PreparedStatement statement = connection.prepareStatement(
                formatStatement("SELECT * FROM %flags%"))) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        final int townId = resultSet.getInt("town_id");
                        final Town town = towns.stream().filter(t -> t.getId() == townId).findFirst().orElseThrow();
                        final Claim.Type type = Claim.Type.values()[resultSet.getInt("chunk_type")];
                        town.getRules().put(type, Rules.from(Map.of(
                            Flag.Defaults.EXPLOSION_DAMAGE.getName(), resultSet.getBoolean("explosion_damage"),
                            Flag.Defaults.FIRE_DAMAGE.getName(), resultSet.getBoolean("fire_damage"),
                            Flag.Defaults.MOB_GRIEFING.getName(), resultSet.getBoolean("mob_griefing"),
                            Flag.Defaults.MONSTER_SPAWNING.getName(), resultSet.getBoolean("monster_spawning"),
                            Flag.Defaults.PVP.getName(), resultSet.getBoolean("pvp"),
                            Flag.Defaults.PUBLIC_INTERACT_ACCESS.getName(), resultSet.getBoolean("public_interact_access"),
                            Flag.Defaults.PUBLIC_CONTAINER_ACCESS.getName(), resultSet.getBoolean("public_container_access"),
                            Flag.Defaults.PUBLIC_BUILD_ACCESS.getName(), resultSet.getBoolean("public_build_access"),
                            Flag.Defaults.PUBLIC_FARM_ACCESS.getName(), resultSet.getBoolean("public_farm_access")
                        )));
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return towns;
    }


    // Get a map of world names to converted claim worlds
    @NotNull
    protected Map<ServerWorld, ClaimWorld> getConvertedClaimWorlds() {
        final Map<ServerWorld, ClaimWorld> claimWorlds = plugin.getDatabase().getAllClaimWorlds();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(formatStatement("""
                SELECT %towns%.`name` AS `town_name`, `world`, `server`, `chunk_x`, `chunk_z`, `chunk_type` FROM %claims%
                INNER JOIN %towns% ON %claims%.`town_id`=%towns%.`id`"""))) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        final String worldName = resultSet.getString("world");
                        final String serverName = resultSet.getString("server");
                        final ServerWorld serverWorld = new ServerWorld(serverName, World.of(
                            new UUID(0, 0),
                            worldName, determineEnvironment(worldName)));

                        final Optional<ClaimWorld> world = claimWorlds.entrySet()
                            .stream()
                            .filter(e -> e.getKey().equals(serverWorld))
                            .map(Map.Entry::getValue)
                            .findFirst();

                        if (world.isEmpty()) {
                            plugin.log(Level.WARNING, "Could not find claim world for " + serverWorld + "! " +
                                "Are all your servers online and running the latest HuskTowns version?");
                            continue;
                        }
                        final ClaimWorld claimWorld = world.get();
                        final Claim claim = Claim.at(Chunk.at(resultSet.getInt("chunk_x"),
                            resultSet.getInt("chunk_z")));
                        claim.setType(Claim.Type.values()[resultSet.getInt("chunk_type")]);

                        final String townName = resultSet.getString("town_name");
                        final Optional<Town> town = plugin.getTowns().stream()
                            .filter(t -> t.getName().equals(townName))
                            .findFirst();
                        if (town.isEmpty()) {
                            plugin.log(Level.WARNING, "Could not find a town with the name " + townName + "; " +
                                "it may have been skipped.");
                            continue;
                        }

                        claimWorld.addClaim(new TownClaim(town.get(), claim));
                        claimWorlds.replaceAll((k, v) -> k.equals(serverWorld) ? claimWorld : v);
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return claimWorlds;
    }

    protected List<User> getConvertedUsers() {
        final List<User> users = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                formatStatement("SELECT * FROM %players%"))) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        users.add(User.of(UUID.fromString(resultSet.getString("uuid")),
                            resultSet.getString("username")));
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return users;
    }

    @NotNull
    private String determineEnvironment(@NotNull String worldName) {
        if (worldName.endsWith("_nether")) {
            return "nether";
        }
        if (worldName.endsWith("_the_end")) {
            return "end";
        }
        return "normal";
    }

    @NotNull
    private String clearLegacyFormatting(@NotNull String existingMetadata) {
        return existingMetadata.replaceAll("&[a-zA-Z0-9]", "");
    }

    @NotNull
    private Connection getConnection() throws SQLException {
        final Database.Type type = getParameter(Parameters.LEGACY_DATABASE_TYPE.name())
            .map(String::toUpperCase).map(Database.Type::valueOf).orElse(Database.Type.MYSQL);
        final String host = getParameter(Parameters.LEGACY_DATABASE_HOST.name()).orElse("localhost");
        final int port = getParameter(Parameters.LEGACY_DATABASE_PORT.name()).map(Integer::parseInt).orElse(3306);
        final String database = getParameter(Parameters.LEGACY_DATABASE_NAME.name()).orElse("HuskTowns");
        final String username = getParameter(Parameters.LEGACY_DATABASE_USERNAME.name()).orElse("root");
        final String password = getParameter(Parameters.LEGACY_DATABASE_PASSWORD.name()).orElse("");

        if (type == Database.Type.MYSQL || type == Database.Type.MARIADB) {
            return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
        }
        return DriverManager.getConnection("jdbc:sqlite:" + new File(plugin.getDataFolder(), "HuskTownsData.db").getAbsolutePath());
    }

    @NotNull
    private String formatStatement(@NotNull String statement) {
        return statement.replaceAll("%players%", getParameter(Parameters.LEGACY_PLAYERS_TABLE.name()).orElse("husktowns_players"))
            .replaceAll("%towns%", getParameter(Parameters.LEGACY_TOWNS_TABLE.name()).orElse("husktowns_towns"))
            .replaceAll("%plot_members%", getParameter(Parameters.LEGACY_PLOT_MEMBERS_TABLE.name()).orElse("husktowns_plot_members"))
            .replaceAll("%claims%", getParameter(Parameters.LEGACY_CLAIMS_TABLE.name()).orElse("husktowns_claims"))
            .replaceAll("%flags%", getParameter(Parameters.LEGACY_FLAGS_TABLE.name()).orElse("husktowns_flags"))
            .replaceAll("%locations%", getParameter(Parameters.LEGACY_LOCATIONS_TABLE.name()).orElse("husktowns_locations"))
            .replaceAll("%bonuses%", getParameter(Parameters.LEGACY_BONUSES_TABLE.name()).orElse("husktowns_bonus"));
    }

    /**
     * Parameters for carrying out a legacy migration
     */
    protected enum Parameters {
        LEGACY_DATABASE_TYPE("MySQL"),
        LEGACY_DATABASE_HOST("localhost"),
        LEGACY_DATABASE_PORT("3306"),
        LEGACY_DATABASE_NAME("HuskTowns"),
        LEGACY_DATABASE_USERNAME("root"),
        LEGACY_DATABASE_PASSWORD("pa55w0rd"),
        LEGACY_PLAYERS_TABLE("husktowns_players"),
        LEGACY_TOWNS_TABLE("husktowns_towns"),
        LEGACY_PLOT_MEMBERS_TABLE("husktowns_plot_members"),
        LEGACY_CLAIMS_TABLE("husktowns_claims"),
        LEGACY_FLAGS_TABLE("husktowns_flags"),
        LEGACY_LOCATIONS_TABLE("husktowns_locations"),
        LEGACY_BONUSES_TABLE("husktowns_bonus");

        private final String defaultValue;

        Parameters(@NotNull String defaultValue) {
            this.defaultValue = defaultValue;
        }

        @NotNull
        private String getDefault() {
            return defaultValue;
        }

    }

}
