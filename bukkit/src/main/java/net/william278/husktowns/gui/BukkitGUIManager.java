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

import kalyaclaims.gui.GUIManager;
import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.gui.deeds.DeedsGui;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Role;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.item.impl.controlitem.PageItem;
import xyz.xenondevs.invui.window.Window;

import java.util.List;
import java.util.Optional;


public class BukkitGUIManager implements GUIManager {

    private final BukkitHuskTowns plugin;

    public BukkitGUIManager(BukkitHuskTowns plugin) {
        this.plugin = plugin;
    }


    @Override
    public void openTownGUI(CommandUser executor, Town town) {
        System.out.println("Opening town GUI");

        Gui gui = Gui.normal()
                .setStructure("##MMMMM##",
                        "##mmmmm##",
                        "##sssss##",
                        "#########",
                        "#ggwwwff#",
                        "##jjjjj##",
                        "##ccccc##",
                        "###lll###",
                        "#########")
                .addIngredient('M', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.PAPER)
                                .setDisplayName("§bLands Manager");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {

                    }
                })
                .addIngredient('m', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.PAPER)
                                .setDisplayName("§aMembers");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {

                    }
                })
                .addIngredient('s', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.PAPER)
                                .setDisplayName("§6Structures");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {

                    }
                })
                .addIngredient('g', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.PAPER)
                                .setDisplayName("§6Greetings");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {

                    }
                })
                .addIngredient('f', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.PAPER)
                                .setDisplayName("§6Farewell");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {

                    }
                })
                .addIngredient('j', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.PAPER)
                                .setDisplayName("§eJoin guild");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {

                    }
                })
                .addIngredient('c', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.PAPER)
                                .setDisplayName("§6Create guild");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {

                    }
                })
                .addIngredient('l', new AbstractItem() {
                    @Override
                    public ItemProvider getItemProvider() {
                        return new ItemBuilder(Material.PAPER)
                                .setDisplayName("§cGuild lookup");
                    }

                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {

                    }
                }).build();
        if (executor instanceof OnlineUser player)
            plugin.getScheduler().globalRegionalScheduler()
                    .run(() -> Window.merged()
                            .setGui(gui)
                            .setTitle("Town GUI")
                            .open(plugin.getServer().getPlayer(player.getUuid())));


    }

    @Override
    public void openTownListGUI(CommandUser executor, Town town) {
        System.out.println("Opening town list GUI");
        Gui gui = PagedGui.items()
                .setStructure("xxxxxxxxx",
                        "xxxxxxxxx",
                        "xxxxxxxxx",
                        "<xxfffxx>")
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL) // where paged items should be put
                .addIngredient('<', new PageItem(false) {
                    @Override
                    public ItemProvider getItemProvider(PagedGui<?> gui) {
                        return new ItemBuilder(Material.ARROW);
                    }
                })
                .addIngredient('>', new PageItem(true) {
                    @Override
                    public ItemProvider getItemProvider(PagedGui<?> gui) {
                        return new ItemBuilder(Material.ARROW);
                    }
                })
                .setContent(BukkitHuskTowns.getInstance().getTowns().stream()
                        .map(town1 -> (Item) new TownItem(town1))
                        .toList())
                .build();
        if (executor instanceof OnlineUser player)
            plugin.getScheduler().globalRegionalScheduler()
                    .run(() -> Window.single()
                            .setGui(gui)
                            .setTitle("Town list")
                            .open(plugin.getServer().getPlayer(player.getUuid())));
    }

    @Override
    public void openDeedsGUI(CommandUser executor, Town town) {
        System.out.println("Opening deeds GUI");
        if (executor instanceof OnlineUser player) {
            DeedsGui dg = new DeedsGui(player, town, plugin);
            plugin.getServer().getScheduler().runTask(plugin, () ->
                    dg.openWindow(plugin.getServer().getPlayer(player.getUuid())));
        }

    }

    @Override
    public void openCensusGUI(CommandUser executor, Town town) {
        PagedGui.Builder<Item> builder = PagedGui.items()
                .setStructure("xxxxxxxxx",
                        "xxxxxxxxx",
                        "xxxxxxxxx",
                        "<xxfffxx>")
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL) // where paged items should be put
                .addIngredient('<', new PageItem(false) {
                    @Override
                    public ItemProvider getItemProvider(PagedGui<?> gui) {
                        return new ItemBuilder(Material.ARROW);
                    }
                })
                .addIngredient('>', new PageItem(true) {
                    @Override
                    public ItemProvider getItemProvider(PagedGui<?> gui) {
                        return new ItemBuilder(Material.ARROW);
                    }
                });
        Gui gui = builder.setContent(town.getMembers().entrySet().stream()
                .map(entry -> {
                    Optional<Role> role = plugin.getRoles().fromWeight(entry.getValue());
                    return role.flatMap(value ->
                                    plugin.getDatabase().getUser(entry.getKey())
                                            .map(user ->
                                                    new Member(user.user(), town, value)))
                            .orElse(null);
                })
                .map(member -> (Item) new MemberItem(member))
                .toList()).build();

        if (executor instanceof OnlineUser player) {
            plugin.getServer().getScheduler().runTask(plugin, () ->
                    Window.single()
                            .setGui(gui)
                            .setTitle("Census")
                            .open(plugin.getServer().getPlayer(player.getUuid())));
        }

        new SimpleItem(new ItemBuilder(Material.ARROW).setDisplayName("§cBack")) {
            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
                plugin.getGUIManager().openTownGUI(executor, town);
            }

            @Override
            public void notifyWindows() {

            }
        };
    }


    private static class TownItem extends AbstractItem {
        private final Town town;

        public TownItem(Town town) {
            super();
            this.town = town;
        }

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.BELL)
                    .setDisplayName(town.getName())
                    .setLegacyLore(List.of(
                            "§7" + town.getMembers().size() + " members",
                            "§7" + town.getClaimCount() + " plots",
                            "§7" + town.getMoney() + " coins",
                            "§7" + town.getSpawn().map(spawn -> "Spawn privacy: " + (spawn.isPublic() ? "public" : "private"))
                                    .orElse("No spawn set")
                    ));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {

        }

    }

    private static class MemberItem extends AbstractItem {
        private final Member member;

        public MemberItem(Member member) {
            super();
            this.member = member;
        }

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.BELL)
                    .setDisplayName(member.user().getUsername())
                    .setLegacyLore(List.of(
                            "§7Role: " + member.role().getName()
                    ));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {

        }

    }

}
