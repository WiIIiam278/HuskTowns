package net.william278.husktowns.gui.census;

import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.gui.GuiSettings;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.AbstractGui;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

public class MemberGui extends AbstractGui {
    private final Member member;

    public MemberGui(OnlineUser executor, MemberItem memberItem) {
        super(9, 3);
        this.member = memberItem.getMember();
        GuiSettings.SingleGuiSettings memberGuiSettings = GuiSettings.getInstance().getMemberGuiSettings();
        applyStructure(new Structure(memberGuiSettings.structure())
                .addIngredient('P', getPromoteButton(memberGuiSettings, executor))
                .addIngredient('D', getDemoteButton(memberGuiSettings, executor))
                .addIngredient('K', getKickButton(memberGuiSettings, executor))

        );
    }

    public void open(Player player) {
        Window.single()
                .setTitle("Member " + member.user().getUsername())
                .setGui(this)
                .open(player);
    }

    private AbstractItem getPromoteButton(GuiSettings.SingleGuiSettings memberGuiSettings, OnlineUser executor) {
        return new SimpleItem(memberGuiSettings.getItem("promoteItem").toItemProvider()) {
            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
                BukkitHuskTowns.getInstance().getManager().towns().promoteMember(executor, member.user().getUsername());
            }
        };
    }

    private AbstractItem getDemoteButton(GuiSettings.SingleGuiSettings memberGuiSettings, OnlineUser executor) {
        return new SimpleItem(memberGuiSettings.getItem("demoteItem").toItemProvider()) {
            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
                BukkitHuskTowns.getInstance().getManager().towns().demoteMember(executor, member.user().getUsername());
            }
        };
    }

    private AbstractItem getKickButton(GuiSettings.SingleGuiSettings memberGuiSettings, OnlineUser executor) {
        return new SimpleItem(memberGuiSettings.getItem("kickItem").toItemProvider()) {
            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
                BukkitHuskTowns.getInstance().getManager().towns().removeMember(executor, member.user().getUsername());
            }
        };
    }


}
