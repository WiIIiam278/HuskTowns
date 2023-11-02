package net.william278.husktowns.gui.info;


import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.gui.BukkitGuiManager;
import net.william278.husktowns.gui.GuiSettings;
import net.william278.husktowns.town.Spawn;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.SavedUser;
import net.william278.husktowns.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.Optional;

public class TownInfoGui {

    private final Gui gui;
    private final Town town;

    public TownInfoGui(Town town, BukkitGuiManager guiManager) {
        GuiSettings.SingleGuiSettings townInfo = guiManager.getGuiSettings().getTownInfoGuiSettings();
        this.town = town;
        this.gui = Gui.normal()
                .setStructure(townInfo.structure())
                .addIngredient('M', new SimpleItem(townInfo.getItem("memberItem").toItemProvider()) {
                    @Override
                    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
                        guiManager.openCensusGUI(BukkitUser.adapt(player), town);
                    }
                })
                .addIngredient('I', new InfoItem(town, townInfo))
                .addIngredient('T', new SpawnTpItem(town, guiManager)).build();
    }

    public void open(Player player) {
        Window.single()
                .setTitle("Town Info: " + town.getName())
                .setGui(gui)
                .open(player);
    }

    private static class InfoItem extends AbstractItem {
        private final Town town;
        private final GuiSettings.SingleGuiSettings guiSettings;

        public InfoItem(Town town, GuiSettings.SingleGuiSettings guiSettings) {
            this.town = town;
            this.guiSettings = guiSettings;
        }


        @Override
        public ItemProvider getItemProvider() {
            return guiSettings.getItem("infoItem").toItemProvider(
                    "%town_name%", town.getName(),
                    "%town_owner%",
                    BukkitHuskTowns.getInstance().getDatabase()
                            .getUser(town.getMayor())
                            .map(SavedUser::user)
                            .map(User::getUsername)
                            .orElse("Unknown"),
                    "%town_members%", String.valueOf(town.getMembers().size()),
                    "%town_claims%", String.valueOf(town.getClaimCount()),
                    "%town_money%", String.valueOf(town.getMoney()),
                    "%town_spawn%", town.getSpawn().map(spawn -> spawn.isPublic() ? "Public" : "Private").orElse("Not set"),
                    "%town_level%", String.valueOf(town.getLevel())
            );
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        }
    }


    private static class SpawnTpItem extends AbstractItem {
        private final Town town;
        private final BukkitGuiManager guiManager;

        public SpawnTpItem(Town town, BukkitGuiManager guiManager) {
            this.town = town;
            this.guiManager = guiManager;
        }

        @Override
        public ItemProvider getItemProvider() {
            Optional<Spawn> spawnOptional = town.getSpawn();
            if (spawnOptional.isPresent()) {
                Spawn spawn = spawnOptional.get();
                return guiManager.getGuiSettings().getTownInfoGuiSettings().getItem("publicSpawnItem").toItemProvider(
                        "%spawn_x%", String.valueOf(spawn.getPosition().getX()),
                        "%spawn_y%", String.valueOf(spawn.getPosition().getY()),
                        "%spawn_z%", String.valueOf(spawn.getPosition().getZ()),
                        "%world%", spawn.getPosition().getWorld().getName(),
                        "%server%", spawn.getServer()
                );
            }
            return guiManager.getGuiSettings().getTownInfoGuiSettings().getItem("privateSpawnItem").toItemProvider();
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            guiManager.getPlugin().getManager().towns().teleportToTownSpawn(BukkitUser.adapt(player), town.getName());
        }
    }
}
