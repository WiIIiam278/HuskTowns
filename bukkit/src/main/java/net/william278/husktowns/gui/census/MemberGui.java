package net.william278.husktowns.gui.census;

import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.AbstractGui;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

public class MemberGui extends AbstractGui {
    private final Member member;

    public MemberGui(OnlineUser executor, MemberItem memberItem) {
        super(9, 3);
        this.member = memberItem.getMember();
        applyStructure(new Structure(
                "#########",
                "#F#PPPDDD",
                "###CCCKKK")
                .addIngredient('P', getPromoteButton(executor))
                .addIngredient('D', getDemoteButton(executor))
                .addIngredient('K', getKickButton(executor))

        );
    }

    public void open(Player player) {
        Window.single()
                .setTitle("Member " + member.user().getUsername())
                .setGui(this)
                .open(player);
    }

    private AbstractItem getPromoteButton(OnlineUser executor) {
        return new SimpleItem(new ItemBuilder(Material.BEACON)) {
            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
                BukkitHuskTowns.getInstance().getManager().towns().promoteMember(executor, member.user().getUsername());
            }
        };
    }

    private AbstractItem getDemoteButton(OnlineUser executor) {
        return new SimpleItem(new ItemBuilder(Material.CARVED_PUMPKIN)) {
            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
                BukkitHuskTowns.getInstance().getManager().towns().demoteMember(executor, member.user().getUsername());
            }
        };
    }

    private AbstractItem getKickButton(OnlineUser executor) {
        return new SimpleItem(new ItemBuilder(Material.BARRIER)) {
            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
                BukkitHuskTowns.getInstance().getManager().towns().removeMember(executor, member.user().getUsername());
            }
        };
    }


}
