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

package net.william278.husktowns.config;

import com.google.common.collect.Lists;
import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.format.TextColor;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.database.Database;
import net.william278.husktowns.network.Broker;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Plugin settings, read from config.yml
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Settings {

    protected static final String CONFIG_HEADER = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃       HuskTowns Config       ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ Information: https://william278.net/project/husktowns
        ┣╸ Config Help: https://william278.net/docs/husktowns/config-files/
        ┗╸ Documentation: https://william278.net/docs/husktowns""";

    // Top-level settings
    @Comment("Locale of the default language file to use. Docs: https://william278.net/docs/husktowns/translations")
    private String language = Locales.DEFAULT_LOCALE;

    @Comment("Whether to automatically check for plugin updates on startup")
    private boolean checkForUpdates = true;

    @Comment("Aliases to use for the /town command.")
    private List<String> aliases = List.of(
        "t"
    );

    // Database settings
    @Comment("Database settings")
    private DatabaseSettings database = new DatabaseSettings();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DatabaseSettings {

        @Comment("Type of database to use (SQLITE, MYSQL, MARIADB)")
        private Database.Type type = Database.Type.SQLITE;

        @Comment("Specify credentials here for your MYSQL or MARIADB database")
        private DatabaseCredentials credentials = new DatabaseCredentials();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class DatabaseCredentials {
            private String host = "localhost";
            private int port = 3306;
            private String database = "HuskTowns";
            private String username = "root";
            private String password = "pa55w0rd";
            private String parameters = String.join("&",
                "?autoReconnect=true", "useSSL=false",
                "useUnicode=true", "characterEncoding=UTF-8");
        }

        @Comment("MYSQL / MARIADB database Hikari connection pool properties. Don't modify this unless you know what you're doing!")
        private PoolOptions connectionPool = new PoolOptions();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class PoolOptions {
            private int size = 10;
            private int idle = 10;
            private long lifetime = 1800000;
            private long keepalive = 0;
            private long timeout = 5000;
        }

        @Comment("Names of tables to use on your database. Don't modify this unless you know what you're doing!")
        @Getter(AccessLevel.NONE)
        private Map<String, String> tableNames = Database.TableName.getDefaults();

        @NotNull
        public String getTableName(@NotNull Database.TableName tableName) {
            return tableNames.getOrDefault(tableName.name().toLowerCase(Locale.ENGLISH), tableName.getDefaultName());
        }

    }

    // Cross-server settings
    @Comment("Cross-server settings")
    private CrossServerSettings crossServer = new CrossServerSettings();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CrossServerSettings {

        @Comment("Whether to enable cross-server mode")
        private boolean enabled = false;

        @Comment({"The cluster ID, for if you're networking multiple separate groups of HuskTowns-enabled servers.",
            "Do not change unless you know what you're doing"})
        private String clusterId = "main";

        @Comment("Type of network message broker to ues for data synchronization (PLUGIN_MESSAGE or REDIS)")
        private Broker.Type brokerType = Broker.Type.PLUGIN_MESSAGE;

        @Comment("Settings for if you're using REDIS as your message broker")
        private RedisSettings redis = new RedisSettings();

        @Getter
        @Configuration
        @NoArgsConstructor
        public static class RedisSettings {
            private String host = "localhost";
            private int port = 6379;
            @Comment("Password for your Redis server. Leave blank if you're not using a password.")
            private String password = "";
            private boolean useSsl = false;

            @Comment({"Settings for if you're using Redis Sentinels.",
                "If you're not sure what this is, please ignore this section."})
            private SentinelSettings sentinel = new SentinelSettings();

            @Getter
            @Configuration
            @NoArgsConstructor
            public static class SentinelSettings {
                private String masterName = "";
                @Comment("List of host:port pairs")
                private List<String> nodes = Lists.newArrayList();
                private String password = "";
            }
        }
    }


    // General settings
    @Comment("Cross-server settings")
    private GeneralSettings general = new GeneralSettings();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GeneralSettings {

        @Comment("How many items should be displayed per-page in chat menu lists")
        private int listItemsPerPage = 6;

        @Comment("Which item to use for the inspector tool; the item that displays claim information when right-clicked.")
        private String inspectorTool = "minecraft:stick";

        @Comment("How far away the inspector tool can be used from a claim. (in blocks)")
        private int maxInspectionDistance = 80;

        @Comment("The slot to display claim entry/teleportation notifications in. (ACTION_BAR, CHAT, TITLE, SUBTITLE, NONE)")
        private Locales.Slot notificationSlot = Locales.Slot.ACTION_BAR;

        @Comment("The width and height of the claim map displayed in chat when running the /town map command.")
        private int claimMapWidth = 9;
        private int claimMapHeight = 9;

        @Comment("The claim and wilderness characters for the claim map displayed in chat when running the /town map command.")
        private char claimMapClaimChar = '⬛';
        private char claimMapWildernessChar = '⬜';

        @Comment("Whether town spawns should be automatically created when a town's first claim is made.")
        private boolean firstClaimAutoSetspawn = false;

        @Comment("Whether to allow players to attack other players in their town.")
        private boolean allowFriendlyFire = false;

        @Comment("A list of world names where claims cannot be created.")
        private List<String> unclaimableWorlds = List.of(
            "world_nether",
            "world_the_end"
        );

        @Comment("A list of town names that cannot be used.")
        private List<String> prohibitedTownNames = List.of(
            "Administrators",
            "Moderators",
            "Mods",
            "Staff",
            "Server"
        );

        @Comment("Adds special advancements for town progression. Docs: https://william278.net/docs/husktowns/advancements/")
        private boolean doAdvancements = false;

        @Comment("Enable economy features. Requires Vault and a compatible economy plugin. " +
            "If disabled, or if Vault is not installed, the built-in town points currency will be used instead. " +
            "Docs: https://william278.net/docs/husktowns/hooks")
        private boolean economyHook = true;

        @Comment("Hook with LuckPerms to provide town permission contexts. Docs: https://william278.net/docs/husktowns/hooks")
        private boolean luckpermsContextsHook = true;

        @Comment("Hook with PlaceholderAPI to provide placeholders. Docs: https://william278.net/docs/husktowns/hooks")
        private boolean placeholderapiHook = true;

        @Comment("Use HuskHomes for improved teleportation. Docs: https://william278.net/docs/husktowns/hooks")
        private boolean huskhomesHook = true;

        @Comment("Show town information on your Player Analytics web panel. Docs: https://william278.net/docs/husktowns/hooks")
        private boolean planHook = true;

        @Comment("Restrict claiming in WorldGuard regions with a flag. Docs: https://william278.net/docs/husktowns/hooks")
        private boolean worldGuardHook = true;

        @Comment("Show town information on your server Dynmap, BlueMap or Pl3xMap. Docs: https://william278.net/docs/husktowns/hooks")
        private MapHookSettings webMapHook = new MapHookSettings();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class MapHookSettings {
            @Comment("Enable hooking into web map plugins")
            private boolean enabled = true;

            @Comment("The name of the marker set to use for claims on your web map")
            private String markerSetName = "Claims";
        }

        public boolean isUnclaimableWorld(@NotNull World world) {
            return unclaimableWorlds.stream().anyMatch(world.getName()::equalsIgnoreCase);
        }

        public boolean isTownNameAllowed(@NotNull String name) {
            return prohibitedTownNames.stream().noneMatch(name::equalsIgnoreCase);
        }
    }


    // General settings
    @Comment("Town settings")
    private TownSettings towns = new TownSettings();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TownSettings {

        @Comment("Whether town names should be restricted by a regex. Set this to false to allow full UTF-8 names.")
        private boolean restrictTownNames = true;

        @Comment("Regex which town names must match. Names have a hard min/max length of 3-16 characters")
        private String townNameRegex = "[a-zA-Z0-9-_]*";

        @Comment("Whether town bios/greetings/farewells should be restricted. Set this to false to allow full UTF-8.")
        private boolean restrictTownBios = true;

        @Comment("Regex which town bios/greeting/farewells must match. A hard max length of 256 characters is enforced")
        private String townMetaRegex = "\\A\\p{ASCII}*\\z";

        @Comment("Require the level 1 cost as collateral when creating a town (this cost is otherwise ignored)")
        private boolean requireFirstLevelCollateral = false;

        @Comment("The minimum distance apart towns must be, in chunks")
        private int minimumChunkSeparation = 0;

        @Comment("Require towns to have all their claims adjacent to each other")
        private boolean requireClaimAdjacency = false;

        @Comment("Whether to spawn particle effects when crop growth or mob spawning is boosted by a town's level")
        private boolean spawnBoostParticles = true;

        @Comment("Which particle effect to use for crop growth and mob spawning boosts")
        private String boostParticle = "spell_witch";


        // Town relations settings
        @Comment("Relations settings")
        private RelationsSettings relations = new RelationsSettings();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class RelationsSettings {

            @Comment("Enable town relations (alliances and enemies). " +
                "Docs: https://william278.net/docs/husktowns/relations/")
            private boolean enabled = true;

            @Comment("Town War settings")
            private WarSettings wars = new WarSettings();

            @Getter
            @Configuration
            @NoArgsConstructor(access = AccessLevel.PRIVATE)
            public static class WarSettings {

                @Comment("Allow mutual enemy towns to agree to go to war. Requires town relations to be enabled. " +
                    "Wars consist of a battle between members, to take place at the spawn of the defending town" +
                    "Docs: https://william278.net/docs/husktowns/wars/")
                private boolean enabled = false;

                @Comment("The number of hours before a town can be involved with another war after finishing one")
                private long cooldown = 48;

                @Comment("How long before pending declarations of war expire")
                private long declarationExpiry = 10;

                @Comment("The minimum wager for a war. This is the amount of money each town must pay to participate in a war." +
                    " The winner of the war will receive both wagers.")
                private double minimumWager = 5000;

                @Comment("The color of the boss bar displayed during a war")
                private BossBar.Color bossBarColor = BossBar.Color.RED;

                @Comment("The minimum number of members online in a town for it to be able to participate in a war (%).")
                private double requiredOnlineMembership = 50.0;

                @Comment("The radius around the defending town's spawn, in blocks, where battle can take place. (Min: 16)")
                private long warZoneRadius = 128;

            }
        }


        // Admin Town settings
        @Comment("Admin Town settings for changing how admin claims look")
        private AdminTownSettings adminTown = new AdminTownSettings();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class AdminTownSettings {

            private String name = "Admin";

            @Getter(AccessLevel.NONE)
            private String color = "#ff0000";

            @NotNull
            public TextColor getColor() {
                return Objects.requireNonNull(
                    TextColor.fromHexString(color),
                    "Invalid hex color code for admin town"
                );
            }
        }

        // Inactive claim pruning settings
        @Comment("Settings for town pruning")
        private TownPruningSettings pruneInactiveTowns = new TownPruningSettings();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class TownPruningSettings {

            @Comment("Delete towns on startup who have had no members online within a certain number of days. " +
                "Docs: https://william278.net/docs/husktowns/inactive-town-pruning/")
            private boolean pruneOnStartup = false;

            @Comment("The number of days a town can be inactive before it will be deleted")
            private int pruneAfterDays = 90;

        }
    }

}
