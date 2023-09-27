package dev.unnm3d.kalyaclaims.gui.deeds;

import fr.skytasul.glowingentities.GlowingBlocks;
import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
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

    public AerialView(OnlineUser onlineUser, BukkitHuskTowns plugin) {

        this.plugin = plugin;
        Optional<ClaimWorld> optClaimWorld = plugin.getClaimWorld(onlineUser.getWorld());
        nearClaims = optClaimWorld.map(claimWorld -> claimWorld.getClaimsNear(onlineUser.getChunk(), 5, plugin)).orElse(null);

    }

    private static void displayChunk(GlowingBlocks glowingBlocks, TownClaim townClaim, Player player, boolean removeGlow) throws ReflectiveOperationException {
        int minX = townClaim.claim().getChunk().getX() << 4;
        int minZ = townClaim.claim().getChunk().getZ() << 4;
        int maxX = minX + 15;
        int maxZ = minZ + 15;

        ChatColor color = switch (townClaim.claim().getType()) {
            case CLAIM -> townClaim.claim().getChunk().getX() + townClaim.claim().getChunk().getZ() % 2 == 0
                    ? ChatColor.DARK_AQUA : ChatColor.BLUE;
            case PLOT -> townClaim.claim().getChunk().getX() + townClaim.claim().getChunk().getZ() % 2 == 0
                    ? ChatColor.DARK_GREEN : ChatColor.GREEN;
            case FARM -> townClaim.claim().getChunk().getX() + townClaim.claim().getChunk().getZ() % 2 == 0
                    ? ChatColor.GOLD : ChatColor.YELLOW;
        };
        if (!townClaim.town().getMembers().containsKey(player.getUniqueId())) color = ChatColor.RED;

        List<Block> blocks = new ArrayList<>();
        //Bottom left
        int y = (int) (player.getLocation().getY() - 25);
        blocks.add(player.getWorld().getBlockAt(minX, y, minZ));
        blocks.add(player.getWorld().getBlockAt(minX, y, minZ + 1));
        blocks.add(player.getWorld().getBlockAt(minX + 1, y, minZ));
        //Bottom right
        blocks.add(player.getWorld().getBlockAt(maxX, y, minZ));
        blocks.add(player.getWorld().getBlockAt(maxX, y, minZ + 1));
        blocks.add(player.getWorld().getBlockAt(maxX - 1, y, minZ));
        //Top left
        blocks.add(player.getWorld().getBlockAt(minX, y, maxZ));
        blocks.add(player.getWorld().getBlockAt(minX, y, maxZ - 1));
        blocks.add(player.getWorld().getBlockAt(minX + 1, y, maxZ));
        //Top right
        blocks.add(player.getWorld().getBlockAt(maxX, y, maxZ));
        blocks.add(player.getWorld().getBlockAt(maxX, y, maxZ - 1));
        blocks.add(player.getWorld().getBlockAt(maxX - 1, y, maxZ));

        for (Block block : blocks) {
            try {
                if (!removeGlow) glowingBlocks.setGlowing(block, player, color);
                else glowingBlocks.unsetGlowing(block, player);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public ItemProvider getItemProvider() {
        return new ItemBuilder(Material.FEATHER)
                .setDisplayName("Aerial View");
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        player.closeInventory();
        GlowingBlocks glowingBlocks = new GlowingBlocks(plugin);
        player.teleport(player.getLocation().add(0, 40, 0));
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFlySpeed(0.0f);
        player.setGravity(false);

        //Freeze the player. Shift to unfreeze and go back to ground
        plugin.getServer().getPluginManager().registerEvents(new Listener(player, glowingBlocks, nearClaims), plugin);

        nearClaims.forEach(townClaim ->
        {
            try {
                displayChunk(glowingBlocks,
                        townClaim, player, false);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        });
    }

    private record Listener(Player player, GlowingBlocks glowingBlocks,
                            List<TownClaim> claims) implements org.bukkit.event.Listener {
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
            claims.forEach(townClaim -> {
                try {
                    displayChunk(glowingBlocks,
                            townClaim, player, true);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            });

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
