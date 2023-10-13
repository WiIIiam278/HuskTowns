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
package net.william278.husktowns.gui.deeds;

import net.kyori.adventure.text.Component;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.gui.BukkitGuiManager;
import net.william278.husktowns.gui.PagedItemsGuiAbstract;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeedsGui extends PagedItemsGuiAbstract {
    final OnlineUser onlineUser;
    private final BukkitGuiManager guiManager;
    DeedItem selectedDeed;
    TrustButton trustButton;

    public DeedsGui(OnlineUser onlineUser, Town town, BukkitGuiManager guiManager) {
        super(9, 10, true, 5);
        this.onlineUser = onlineUser;
        this.guiManager = guiManager;
        this.trustButton = new TrustButton(this);
        Structure structure = new Structure(
                guiManager.getGuiSettings().getDeedsGuiSettings().structure())
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('p', getClaimFlagsItem(this.onlineUser, town))
                .addIngredient('t', getClaimDisplay())
                .addIngredient('A', getAbandonClaimButton())
                .addIngredient('T', this.trustButton);
        applyStructure(structure);

        //Add claims to deeds item
        List<Item> deedItems = new ArrayList<>();
        Chunk chunk = onlineUser.getChunk();
        for (int j = -2; j < 3; j++)
            for (int i = -4; i < 5; i++) {
                Optional<TownClaim> claim = guiManager.getPlugin().getClaimAt(Chunk.at(chunk.getX() + i, chunk.getZ() + j), onlineUser.getWorld());
                DeedItem deedItem = new DeedItem(claim.orElse(null), onlineUser, this);
                if (i == 0 && j == 0)
                    this.selectedDeed = deedItem;
                deedItems.add(deedItem);
            }
        setContent(deedItems);
    }

    public void openWindow(Player player) {
        DeedsWindow window = new DeedsWindow(player, new AdventureComponentWrapper(Component.text("Deeds")), this);
        window.open();
    }

    private AbstractItem getClaimDisplay() {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                if (selectedDeed.townClaim == null)
                    return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName("§cUnclaimed");
                return switch (selectedDeed.townClaim.claim().getType()) {
                    case CLAIM -> new ItemBuilder(Material.GRASS_BLOCK).setDisplayName("§aClaim");
                    case PLOT -> new ItemBuilder(Material.OAK_DOOR).setDisplayName("§aPlot");
                    case FARM -> new ItemBuilder(Material.WHEAT).setDisplayName("§aFarm");
                };
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            }
        };
    }

    private AbstractItem getAbandonClaimButton() {
        return new AbstractItem() {
            private int iterations = 0;

            @Override
            public ItemProvider getItemProvider() {
                if (selectedDeed.townClaim == null) return new ItemBuilder(Material.AIR);
                return guiManager.getGuiSettings().getDeedsGuiSettings().getItem("abandonClaimItem").toItemProvider("%iterations%", String.valueOf(iterations));

            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
                if (iterations == 5) {
                    guiManager.getPlugin().getManager().claims().deleteClaimData(onlineUser, selectedDeed.townClaim, onlineUser.getWorld());
                    selectedDeed.townClaim = null;
                }

                iterations++;
                notifyWindows();
            }
        };
    }

    public AbstractItem getClaimFlagsItem(OnlineUser user, Town town) {
        return new AbstractItem() {

            @Override
            public ItemProvider getItemProvider() {
                return guiManager.getGuiSettings().getDeedsGuiSettings().getItem("claimFlagsItem").toItemProvider();
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
                if (town != null)
                    Window.single()
                            .setTitle("Claim flags")
                            .setGui(new ClaimFlagsGui(user, town, guiManager).getGui())
                            .open(player);
            }
        };
    }

    public BukkitGuiManager getGuiManager() {
        return guiManager;
    }

    public static class DeedItem extends AbstractItem {
        TownClaim townClaim;
        private final DeedsGui deedsGui;
        private final OnlineUser viewingUser;

        public DeedItem(TownClaim townClaim, OnlineUser viewingUser, DeedsGui deedsGui) {
            this.townClaim = townClaim;
            this.viewingUser = viewingUser;
            this.deedsGui = deedsGui;
        }

        @Override
        public ItemProvider getItemProvider() {
            if (deedsGui.selectedDeed == this && townClaim != null) {
                return deedsGui.getGuiManager().getGuiSettings().getDeedsGuiSettings().getItem("selectedDeedItem")
                        .toItemProvider(
                                "%chunkx%", String.valueOf(townClaim.claim().getChunk().getX()),
                                "%chunkz%", String.valueOf(townClaim.claim().getChunk().getZ())
                        );
            }

            if (townClaim == null) {
                return deedsGui.getGuiManager().getGuiSettings().getDeedsGuiSettings().getItem("unclaimedItem").toItemProvider();
            }
            if (viewingUser.getChunk().equals(townClaim.claim().getChunk())) {//If it is the chunk you're standing in
                return deedsGui.getGuiManager().getGuiSettings().getDeedsGuiSettings().getItem("viewPointItem").toItemProvider();
            }

            return deedsGui.getGuiManager().getGuiSettings().getDeedsGuiSettings().getItem("claimItem").toItemProvider(
                    "%town%", townClaim.town().getName(),
                    "%chunkx%", String.valueOf(townClaim.claim().getChunk().getX()),
                    "%chunkz%", String.valueOf(townClaim.claim().getChunk().getZ()),
                    "%type%", townClaim.claim().getType().toString().toUpperCase()
            );

        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (clickType.isLeftClick())
                getWindows().stream()
                        .findFirst()
                        .map(window -> (DeedsWindow) window)
                        .ifPresent(deedsWindow -> deedsWindow.selectDeed(this));
            else if (clickType.isRightClick() && townClaim == null) {
                int slot = event.getSlot();
                int xDistance = slot % 9 - 4;
                int zDistance = slot / 9 - 2;
                Chunk viewingChunk = viewingUser.getChunk();

                deedsGui.getGuiManager().getPlugin().getUserTown(viewingUser).ifPresent(member -> {
                    TownClaim tClaim = new TownClaim(member.town(), Claim.at(Chunk.at(viewingChunk.getX() + xDistance, viewingChunk.getZ() + zDistance)));
                    deedsGui.getGuiManager().getPlugin().getManager().claims().createClaimData(
                            viewingUser,
                            tClaim,
                            viewingUser.getWorld());
                    this.townClaim = tClaim;
                    notifyWindows();
                });
            }

        }
    }


}
