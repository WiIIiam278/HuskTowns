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

package net.william278.husktowns;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.google.common.collect.ImmutableList;
import net.william278.husktowns.audit.Action;
import net.william278.husktowns.audit.Log;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.map.MapSquare;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Stream;

@DisplayName("Plugin Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BukkitPluginTests {

    private static ServerMock server;
    private static BukkitHuskTowns plugin;

    @BeforeAll
    @DisplayName("Test Plugin Initialization")
    public static void setUpPlugin() {
        server = MockBukkit.mock();
        server.addSimpleWorld("world");
        plugin = MockBukkit.load(BukkitHuskTowns.class);
    }

    @Order(1)
    @Nested
    @DisplayName("Data Validation Tests")
    public class ValidationTests {

        @ParameterizedTest(name = "{index} (Name: {0})")
        @DisplayName("Test Good Names Pass")
        @MethodSource("getTestTownNames")
        public void testTownNameIsValid(@NotNull String name) {
            Assertions.assertTrue(plugin.getValidator().isLegalTownName(name));
        }

        @ParameterizedTest(name = "{index} (Name: {0})")
        @DisplayName("Test Bad Names Fail")
        @MethodSource("getBadTownNames")
        public void testTownNameIsInvalid(@NotNull String name) {
            Assertions.assertFalse(plugin.getValidator().isLegalTownName(name));
        }

        @NotNull
        private static List<String> getBadTownNames() {
            return BukkitPluginTests.getBadTownNames();
        }

        @NotNull
        private static List<String> getTestTownNames() {
            return BukkitPluginTests.getTestTownNames();
        }
    }

    @Order(2)
    @Nested
    @DisplayName("Level Limit Tests")
    public class LevelLimitTests {

        @ParameterizedTest(name = "To Lv: {index} (Need: {0}, From Lv: {1})")
        @DisplayName("Test Level Up Cost Calculation")
        @MethodSource("getLevelUpCostArguments")
        public void testLevelUpCostCalculation(@NotNull BigDecimal money, int currentLevel) {
            Assertions.assertEquals(money.longValueExact(), plugin.getLevels().getLevelUpCost(currentLevel).longValueExact());
        }

        @NotNull
        private static Stream<Arguments> getLevelUpCostArguments() {
            return Stream.iterate(1, i -> i + 1)
                    .limit(20)
                    .map(i -> Arguments.of(BigDecimal.valueOf(Math.pow(2D, i) * 1000D), i - 1));
        }

    }

    @Order(3)
    @Nested
    @DisplayName("Town Pruning Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class PruningTests {

        private static final Map<String, Long> TEST_DATA = Map.of(
                // Records that should be pruned
                "5000d-Prune", 5000L,
                "110d-Prune", 110L,
                "100d-Prune", 100L,

                // Records that should not be pruned
                "80d-Leave", 80L,
                "40d-Leave", 40L,
                "0d-Leave", 0L
        );
        private static final long PRUNE_AFTER_DAYS = 90L;

        @Order(1)
        @ParameterizedTest(name = "Data: {0}")
        @DisplayName("Test Town Pruning After " + PRUNE_AFTER_DAYS + " Days")
        @MethodSource("getTownPruningArguments")
        public void testTownPruningAfter90Days(@NotNull String townName, long daysToSubtract) {
            boolean shouldPrune = daysToSubtract > PRUNE_AFTER_DAYS;
            final BukkitUser user = BukkitUser.adapt(makePlayer(), plugin);
            Assertions.assertTrue(plugin.findTown(townName).isEmpty());

            final Town town = plugin.getDatabase().createTown(townName, user);
            plugin.getTowns().add(town);
            Assertions.assertFalse(plugin.findTown(townName).isEmpty());

            final OffsetDateTime lastLogin = OffsetDateTime.now().minusDays(daysToSubtract);
            plugin.getDatabase().updateUser(user, lastLogin, Preferences.getDefaults());

            plugin.pruneInactiveTowns(PRUNE_AFTER_DAYS, user);
            Assertions.assertEquals(plugin.findTown(townName).isEmpty(), shouldPrune);
        }

        @Order(2)
        @DisplayName("Test Town Pruning With Multiple Inactive Members")
        @Test
        public void testTownPruningWithMultipleInactiveMembers() {
            final BukkitUser mayor = BukkitUser.adapt(makePlayer(), plugin);
            final BukkitUser member1 = BukkitUser.adapt(makePlayer(), plugin);
            final BukkitUser member2 = BukkitUser.adapt(makePlayer(), plugin);
            Assertions.assertAll(
                    () -> Assertions.assertTrue(plugin.getDatabase().getUser(mayor.getUuid()).isPresent()),
                    () -> Assertions.assertTrue(plugin.getDatabase().getUser(member1.getUuid()).isPresent()),
                    () -> Assertions.assertTrue(plugin.getDatabase().getUser(member2.getUuid()).isPresent())
            );

            final String townName = "InactiveMembers";
            Assertions.assertTrue(plugin.findTown(townName).isEmpty());

            final Town town = plugin.getDatabase().createTown(townName, mayor);
            plugin.getTowns().add(town);
            town.addMember(member1.getUuid(), plugin.getRoles().getDefaultRole());
            town.addMember(member2.getUuid(), plugin.getRoles().getDefaultRole());
            plugin.getManager().updateTownData(mayor, town);
            Assertions.assertAll(
                    () -> Assertions.assertFalse(plugin.findTown(townName).isEmpty()),
                    () -> Assertions.assertTrue(plugin.getUserTown(mayor).isPresent()),
                    () -> Assertions.assertTrue(plugin.getUserTown(member1).isPresent()),
                    () -> Assertions.assertTrue(plugin.getUserTown(member2).isPresent())
            );

            final OffsetDateTime lastLogin = OffsetDateTime.now().minusDays(PRUNE_AFTER_DAYS);
            plugin.getDatabase().updateUser(mayor, lastLogin.minusDays(30), Preferences.getDefaults());
            plugin.getDatabase().updateUser(member1, lastLogin.minusDays(60), Preferences.getDefaults());
            plugin.getDatabase().updateUser(member2, lastLogin.minusDays(90), Preferences.getDefaults());

            Assertions.assertAll(
                    () -> Assertions.assertEquals(1, plugin.pruneInactiveTowns(PRUNE_AFTER_DAYS, mayor)),
                    () -> Assertions.assertTrue(plugin.findTown(townName).isEmpty())
            );
        }

        @Order(3)
        @DisplayName("Test Not Pruning When Some Members Active")
        @Test
        public void testNotPruningWhenSomeMembersActive() {
            final BukkitUser mayor = BukkitUser.adapt(makePlayer(), plugin);
            final BukkitUser member1 = BukkitUser.adapt(makePlayer(), plugin);
            final BukkitUser member2 = BukkitUser.adapt(makePlayer(), plugin);
            Assertions.assertAll(
                    () -> Assertions.assertTrue(plugin.getDatabase().getUser(mayor.getUuid()).isPresent()),
                    () -> Assertions.assertTrue(plugin.getDatabase().getUser(member1.getUuid()).isPresent()),
                    () -> Assertions.assertTrue(plugin.getDatabase().getUser(member2.getUuid()).isPresent())
            );

            final String townName = "SomeActiveMembers";
            Assertions.assertTrue(plugin.findTown(townName).isEmpty());

            final Town town = plugin.getDatabase().createTown(townName, mayor);
            plugin.getTowns().add(town);
            town.addMember(member1.getUuid(), plugin.getRoles().getDefaultRole());
            town.addMember(member2.getUuid(), plugin.getRoles().getDefaultRole());
            plugin.getManager().updateTownData(mayor, town);
            Assertions.assertAll(
                    () -> Assertions.assertFalse(plugin.findTown(townName).isEmpty()),
                    () -> Assertions.assertTrue(plugin.getUserTown(mayor).isPresent()),
                    () -> Assertions.assertTrue(plugin.getUserTown(member1).isPresent()),
                    () -> Assertions.assertTrue(plugin.getUserTown(member2).isPresent())
            );

            final OffsetDateTime lastLogin = OffsetDateTime.now().minusDays(PRUNE_AFTER_DAYS);
            plugin.getDatabase().updateUser(mayor, lastLogin.minusDays(5), Preferences.getDefaults());
            plugin.getDatabase().updateUser(member2, lastLogin.minusDays(15), Preferences.getDefaults());

            plugin.pruneInactiveTowns(PRUNE_AFTER_DAYS, mayor);
            Assertions.assertTrue(plugin.findTown(townName).isPresent());
        }

        private static Stream<Arguments> getTownPruningArguments() {
            return TEST_DATA.entrySet().stream()
                    .map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
        }

    }

    @Order(4)
    @Nested
    @DisplayName("Town Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class TownTests {

        @Order(1)
        @ParameterizedTest(name = "{index} (Town Name: {0})")
        @DisplayName("Test Town Creation")
        @MethodSource("getTownCreationParameters")
        public void testTownCreation(@NotNull String name, @NotNull Player creator) {
            final Town town = plugin.getDatabase().createTown(name, BukkitUser.adapt(creator, plugin));
            Assertions.assertNotNull(town);
            plugin.getTowns().add(town);
        }

        @Order(2)
        @ParameterizedTest(name = "Town ID: {index}")
        @DisplayName("Test Town Member Addition")
        @MethodSource("getTownAndMayorParameters")
        public void testTownMemberAddition(@NotNull Town town, @NotNull Player mayor) {
            final Player playerToAdd = makePlayer();
            town.addMember(playerToAdd.getUniqueId(), plugin.getRoles().getDefaultRole());
            plugin.getManager().updateTownData(BukkitUser.adapt(mayor, plugin), town);
            Assertions.assertTrue(town.getMembers().containsKey(playerToAdd.getUniqueId()));
            Assertions.assertEquals(plugin.getRoles().getDefaultRole().getWeight(),
                    town.getMembers().get(playerToAdd.getUniqueId()));
        }

        @Order(3)
        @ParameterizedTest(name = "Town ID: {index}")
        @DisplayName("Test Town Claiming")
        @MethodSource("getTownAndMayorParameters")
        public void testTownClaiming(@NotNull Town town, @NotNull Player player) {
            final int townIndex = getTestTownNames().indexOf(town.getName());
            final Location location = player.getLocation().add(500D * townIndex, 0, 0);
            player.teleport(location);

            final Chunk chunk = Chunk.at(location.getChunk().getX(), location.getChunk().getZ());
            final TownClaim townClaim = new TownClaim(town, Claim.at(chunk));
            final OnlineUser claimer = BukkitUser.adapt(player, plugin);

            town.setClaimCount(town.getClaimCount() + 1);
            town.getLog().log(Action.of(claimer, Action.Type.CREATE_CLAIM, townClaim.claim().toString()));
            plugin.getManager().updateTownData(claimer, town);

            final org.bukkit.World world = location.getWorld();
            Assertions.assertNotNull(world);

            final World pluginWorld = World.of(world.getUID(), world.getName(), world.getEnvironment().name());
            final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(pluginWorld);
            Assertions.assertTrue(claimWorld.isPresent());

            claimWorld.get().addClaim(townClaim);
            plugin.getDatabase().updateClaimWorld(claimWorld.get());
            Assertions.assertTrue(claimWorld.get().getClaims().containsKey(town.getId()));

            final Optional<TownClaim> claim = plugin.getClaimAt(chunk, pluginWorld);
            Assertions.assertTrue(claim.isPresent());
        }

        @Order(4)
        @ParameterizedTest(name = "Town ID: {index}")
        @DisplayName("Test Claim Map Building")
        @MethodSource("getTownAndMayorParameters")
        public void testClaimMapBuilding(@NotNull Town town, @NotNull Player player) {
            final Chunk chunk = Chunk.at(player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ());
            final World world = World.of(player.getWorld().getUID(), player.getWorld().getName(), player.getWorld().getEnvironment().name());
            final Optional<TownClaim> optionalClaim = plugin.getClaimAt(chunk, world);
            Assertions.assertTrue(optionalClaim.isPresent());

            final TownClaim claim = optionalClaim.get();
            final MapSquare square = MapSquare.claim(chunk, world, claim, plugin);
            Assertions.assertAll(
                    () -> Assertions.assertEquals(claim.town().getId(), town.getId()),
                    () -> Assertions.assertNotNull(square.toComponent().color()),
                    () -> Assertions.assertEquals(Objects.requireNonNull(square.toComponent().color()).asHexString(), town.getColorRgb())
            );
        }

        @NotNull
        private static Stream<Arguments> getTownCreationParameters() {
            return getTestTownNames().stream()
                    .map(name -> Arguments.of(name, makePlayer()));
        }

        @NotNull
        private static Stream<Arguments> getTownAndMayorParameters() {
            return ImmutableList.copyOf(plugin.getTowns()).stream()
                    .map(town -> Arguments.of(town, server.getOnlinePlayers().stream()
                            .filter(user -> user.getUniqueId().equals(town.getMayor()))
                            .findFirst().orElseThrow()));
        }
    }

    @Order(5)
    @Nested
    @DisplayName("Town Schema Update Tests")
    public class TownSchemaTests {

        @Order(1)
        @DisplayName("Test Town Schema Update")
        @Test
        public void testTownSchemaUpdate() {
            final String townJson = String.join("\n", readTestData("town_schema_v0.json"));
            final Town readTown = plugin.getTownFromJson(townJson);
            Assertions.assertNotNull(readTown);
            Assertions.assertAll(
                    () -> Assertions.assertEquals(Town.CURRENT_SCHEMA, readTown.getSchemaVersion()),
                    () -> Assertions.assertEquals("Test", readTown.getBio().orElseThrow()),
                    () -> Assertions.assertEquals("Test", readTown.getGreeting().orElseThrow()),
                    () -> Assertions.assertEquals("Test", readTown.getFarewell().orElseThrow())
            );
        }

        @Order(2)
        @DisplayName("Test Deserializing Town Log with Duplicate Keys")
        @Test
        public void testTownLogSerialization() {
            final String logJson = """
                    {
                        "actions": {
                            "2024-02-04T15:09:34.661609Z": { "action": "CREATE_TOWN" },
                            "2024-02-04T15:09:34.661609Z": { "action": "CREATE_TOWN" }
                        }
                    }""";
            final Log log = plugin.getGson().fromJson(logJson, Log.class);
            Assertions.assertNotNull(log, "Failed to deserialize log with duplicate keys");

            final Map<OffsetDateTime, Action> actions = log.getActions();
            Assertions.assertEquals(1, actions.size(), "Duplicate keys were not removed on load");
        }

    }

    @NotNull
    private static Player makePlayer() {
        final Player player = server.addPlayer();
        if (plugin.getDatabase().getUser(player.getUniqueId()).isEmpty()) {
            plugin.getDatabase().createUser(BukkitUser.adapt(player, plugin), Preferences.getDefaults());
        }
        return player;
    }

    @NotNull
    private static List<String> getBadTownNames() {
        return readTestData("bad_town_names.txt");
    }

    @NotNull
    private static List<String> getTestTownNames() {
        return readTestData("test_town_names.txt");
    }

    @NotNull
    private static List<String> readTestData(@NotNull String fileName) {
        final List<String> townNames = new ArrayList<>();
        try (Scanner scanner = new Scanner(Objects.requireNonNull(BukkitPluginTests.class.getClassLoader()
                .getResourceAsStream(fileName)), StandardCharsets.UTF_8)) {
            while (scanner.hasNextLine()) {
                townNames.add(scanner.nextLine());
            }
        }
        return townNames;
    }

}
