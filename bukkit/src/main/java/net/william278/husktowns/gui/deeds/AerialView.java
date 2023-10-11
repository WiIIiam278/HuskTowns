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

import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AerialView extends AbstractItem {
    private final BukkitHuskTowns plugin;
    private final List<TownClaim> nearClaims;
    final List<ItemDisplay> itemDisplays;

    public AerialView(OnlineUser onlineUser, BukkitHuskTowns plugin) {

        this.plugin = plugin;
        this.itemDisplays = new ArrayList<>();
        Optional<ClaimWorld> optClaimWorld = plugin.getClaimWorld(onlineUser.getWorld());
        nearClaims = optClaimWorld.map(claimWorld -> claimWorld.getClaimsNear(onlineUser.getChunk(), 5, plugin)).orElse(null);

    }

    private void displayChunk(TownClaim townClaim, Player player) throws ReflectiveOperationException {
        int minX = townClaim.claim().getChunk().getX() << 4;
        int minZ = townClaim.claim().getChunk().getZ() << 4;

        Location playerino = new Location(player.getWorld(), minX+8, player.getLocation().getY() - 25, minZ+8);
        ItemDisplay itemDisplay = (ItemDisplay) player.getWorld().spawnEntity(playerino, EntityType.ITEM_DISPLAY);

        itemDisplay.setItemStack(new ItemBuilder(Material.IRON_NUGGET).setCustomModelData(1027).get());
        Transformation t = itemDisplay.getTransformation();
        t.getScale().set(16, 0.1, 16);
        itemDisplay.setBillboard(Display.Billboard.FIXED);
        itemDisplay.setTransformation(t);
        itemDisplay.setVisibleByDefault(true);
        itemDisplay.setViewRange(255f);

        itemDisplays.add(itemDisplay);

    }

    @Override
    public ItemProvider getItemProvider() {
        return new ItemBuilder(Material.FEATHER)
                .setDisplayName("Aerial View");
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        player.closeInventory();
        player.teleport(player.getLocation().add(0, 40, 0));
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFlySpeed(0.0f);
        player.setGravity(false);

        //Freeze the player. Shift to unfreeze and go back to ground
        plugin.getServer().getPluginManager().registerEvents(new Listener(player, this), plugin);

        nearClaims.forEach(townClaim ->
        {
            try {
                displayChunk(townClaim, player);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private record Listener(Player player, AerialView aerialView) implements org.bukkit.event.Listener {
        @EventHandler
        public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
            if (event.getPlayer().equals(player)) { //Cancel only xyz movements
                if (event.getTo().getY() != event.getFrom().getY() ||
                        event.getTo().getX() != event.getFrom().getX() ||
                        event.getTo().getZ() != event.getFrom().getZ())
                    event.setCancelled(true);
            }
        }

        @EventHandler
        public void onPlayerSneak(PlayerToggleSneakEvent event) {
            if (!event.getPlayer().equals(player)) return;

            //Unregister the listener and unglow the blocks
            HandlerList.unregisterAll(this);

            aerialView.itemDisplays.forEach(ItemDisplay::remove);

            //Unfreeze the player
            if (!player.isOp())
                player.setAllowFlight(false);
            player.setFlying(false);
            player.setFlySpeed(0.1f);
            player.setGravity(true);
            player.teleport(player.getLocation().subtract(0, 40, 0));

        }


    }
}
