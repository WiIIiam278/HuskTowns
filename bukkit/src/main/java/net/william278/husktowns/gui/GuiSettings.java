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

package net.william278.husktowns.gui;

import com.google.gson.annotations.Expose;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

import java.util.Arrays;
import java.util.Map;

import static java.util.Map.entry;

public class GuiSettings {
    private static GuiSettings instance = new GuiSettings();

    @Expose
    private SingleGuiSettings deedsGuiSettings = new SingleGuiSettings(
            "Deeds",
            new String[]{"xxxxxxxxx",
                    "xxxxxxxxx",
                    "xxxxxxxxx",
                    "xxxxxxxxx",
                    "xxxxxxxxx",
                    "bb#####pp",
                    "ttt###TTT",
                    "#########",
                    "###AAA###",
                    "#########"},
            Map.ofEntries(
                    entry("claimFlagsItem", new ItemDisplay(Material.BLAZE_POWDER, 0, "Claim Flags", new String[]{"Click to view claim flags"})),
                    entry("claimItem", new ItemDisplay(Material.GRASS_BLOCK, 0, "Normal claim", new String[]{"Click to select"})),
                    entry("plotItem", new ItemDisplay(Material.GRASS_BLOCK, 0, "Plot claim", new String[]{"Click to select"})),
                    entry("farmItem", new ItemDisplay(Material.GRASS_BLOCK, 0, "Normal claim", new String[]{"Click to select"})),
                    entry("unclaimedItem", new ItemDisplay(Material.GRAY_STAINED_GLASS_PANE, 0, "Unclaimed", new String[]{"Click to select"})),
                    entry("viewPointItem", new ItemDisplay(Material.SPRUCE_SIGN, 0, "You are here!", new String[]{"Click to select"})),
                    entry("viewPointSelectedItem", new ItemDisplay(Material.SPRUCE_SIGN, 0, "You are here, it is your claim!", new String[]{"Click to select"})),
                    entry("selectedDeedItem", new ItemDisplay(Material.GREEN_STAINED_GLASS_PANE, 0, "Selected item", new String[]{"Click to select"})),
                    entry("abandonClaimItem", new ItemDisplay(Material.BARRIER, 0, "Abandon Claim", new String[]{"Click to abandon a claim"})),
                    entry("trustItem", new ItemDisplay(Material.BOOK, 0, "Trust", new String[]{"Click to view trust settings"}))
            ));
    @Expose
    private SingleGuiSettings townListGuiSettings = new SingleGuiSettings(
            "Town list",
            new String[]{"xxxxxxxxx",
                    "xxxxxxxxx",
                    "xxxxxxxxx",
                    "<xMmTPpx>"},
            Map.ofEntries(
                    entry("backItem", new ItemDisplay(Material.ARROW, 0, "Back", new String[]{"Click to select"})),
                    entry("forwardItem", new ItemDisplay(Material.GRASS, 0, "Forward", new String[]{"Click to select"})),
                    entry("moneyFilterItem", new ItemDisplay(Material.GOLD_INGOT, 0, "Money", new String[]{"Click to select"})),
                    entry("membersFilterItem", new ItemDisplay(Material.PLAYER_HEAD, 0, "Members", new String[]{"Click to select"})),
                    entry("territoriesFilterItem", new ItemDisplay(Material.GRASS_BLOCK, 0, "Territories", new String[]{"Click to select"})),
                    entry("publicFilterItem", new ItemDisplay(Material.BIRCH_DOOR, 0, "Public", new String[]{"Click to select"})),
                    entry("privateFilterItem", new ItemDisplay(Material.IRON_DOOR, 0, "Private", new String[]{"Click to select"})),
                    entry("selectedFilterItem", new ItemDisplay(Material.BARRIER, 0, "%filter%", new String[]{"You have this filter enabled", "Click to disable"})),
                    entry("townItem", new ItemDisplay(Material.BELL, 0, "%town_name%", new String[]{"Members: %town_members%", "Level: %town_level%", "Territories: %town_claims%", "Money: %town_money%", "Privacy: %town_privacy%"}))
            ));
    @Expose
    private SingleGuiSettings claimFlagsGuiSettings = new SingleGuiSettings(
            "Claim flags",
            new String[]{"abcdefghi",
                    "jklmnopqr",
                    "stuvwxyz{"},
            Map.ofEntries(
                    entry("pvp", new ItemDisplay(Material.IRON_SWORD, 0, "PVP", new String[]{"Click to select", "Current status: %status%"})),
                    entry("explosion_damage", new ItemDisplay(Material.TNT, 0, "Explosion damage", new String[]{"Click to select", "Current status: %status%"})),
                    entry("public_container_access", new ItemDisplay(Material.CHEST, 0, "Public container access", new String[]{"Click to select", "Current status: %status%"})),
                    entry("public_build_access", new ItemDisplay(Material.OAK_PLANKS, 0, "Public build access", new String[]{"Click to select", "Current status: %status%"})),
                    entry("public_farm_access", new ItemDisplay(Material.WHEAT, 0, "Public farm access", new String[]{"Click to select", "Current status: %status%"})),
                    entry("public_interact_access", new ItemDisplay(Material.STONE_BUTTON, 0, "Public interact access", new String[]{"Click to select", "Current status: %status%"})),
                    entry("mob_griefing", new ItemDisplay(Material.ZOMBIE_HEAD, 0, "Mob griefing", new String[]{"Click to select", "Current status: %status%"})),
                    entry("monster_spawning", new ItemDisplay(Material.CREEPER_HEAD, 0, "Mob spawning", new String[]{"Click to select", "Current status: %status%"})),
                    entry("fire_damage", new ItemDisplay(Material.CAMPFIRE, 0, "Fire damage", new String[]{"Click to select", "Current status: %status%"}))
            ));
    @Expose
    private SingleGuiSettings censusGuiSettings = new SingleGuiSettings(
            "Member list",
            new String[]{"xxxxxxxxx",
                    "xxxxxxxxx",
                    "xxxxxxxxx",
                    "<##fff##>"},
            Map.ofEntries(
                    entry("backItem", new ItemDisplay(Material.ARROW, 0, "Back", new String[]{"Click to select"})),
                    entry("forwardItem", new ItemDisplay(Material.GRASS, 0, "Forward", new String[]{"Click to select"})),
                    entry("memberItem", new ItemDisplay(Material.PLAYER_HEAD, 0, "%member_name%", new String[]{"Role %member_role%"}))
            ));
    @Expose
    private SingleGuiSettings memberGuiSettings = new SingleGuiSettings(
            "Manage member: ",
            new String[]{"#########",
                    "#P##D##K#",
                    "#########"},
            Map.ofEntries(
                    entry("promoteItem", new ItemDisplay(Material.GOLD_INGOT, 0, "Promote", new String[]{"Click to promote this member"})),
                    entry("demoteItem", new ItemDisplay(Material.IRON_INGOT, 0, "Demote", new String[]{"Click to demote this member"})),
                    entry("kickItem", new ItemDisplay(Material.BARRIER, 0, "Kick", new String[]{"Click to kick this member"}))
            ));

    public static GuiSettings getInstance() {
        return instance;
    }

    public static void setInstance(GuiSettings instance) {
        GuiSettings.instance = instance;
    }


    public SingleGuiSettings getDeedsGuiSettings() {
        return deedsGuiSettings;
    }

    public SingleGuiSettings getTownListGuiSettings() {
        return townListGuiSettings;
    }

    public SingleGuiSettings getClaimFlagsGuiSettings() {
        return claimFlagsGuiSettings;
    }

    public SingleGuiSettings getCensusGuiSettings() {
        return censusGuiSettings;
    }

    public SingleGuiSettings getMemberGuiSettings() {
        return memberGuiSettings;
    }


    public record SingleGuiSettings(@Expose String title, @Expose String[] structure,
                                    @Expose Map<String, ItemDisplay> items) {
        public ItemDisplay getItem(String key) {
            return items.getOrDefault(key, new ItemDisplay(Material.BREAD, 0, "You have not set " + key + " item", new String[]{}));
        }
    }

    public record ItemDisplay(@Expose Material material, @Expose int modelData, @Expose String displayName,
                              @Expose String[] lore) {
        public ItemProvider toItemProvider(String... replacements) {
            String replaceableDisplayName = this.displayName;
            for (int i = 1; i < replacements.length; i = i + 2) {
                replaceableDisplayName = replaceableDisplayName.replace(replacements[i - 1], ChatColor.translateAlternateColorCodes('&', replacements[i]));
                for (int i1 = 0; i1 < lore.length; i1++) {
                    lore[i1] = lore[i1].replace(replacements[i - 1], ChatColor.translateAlternateColorCodes('&', replacements[i]));
                }
            }
            return new ItemBuilder(material).setCustomModelData(modelData).setDisplayName(replaceableDisplayName).setLegacyLore(Arrays.asList(lore));
        }
    }
}
