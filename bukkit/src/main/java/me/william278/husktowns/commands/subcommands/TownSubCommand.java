package me.william278.husktowns.commands.subcommands;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.town.TownRole;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class TownSubCommand extends SubCommand {

    //todo move admin functions into the /townadmin command

    private static final HuskTowns plugin = HuskTowns.getInstance();

    public String description;
    public TownRole requiredRole;
    public String privilegeErrorCode;

    public TownSubCommand(String subCommand, String permissionNode, String usage, String... aliases) {
        super("town", subCommand, permissionNode, usage, aliases);
        this.description = MessageManager.getRawMessage("town_" + subCommand + "_subcommand_description");
    }

    public TownSubCommand(String subCommand, String permissionNode, String usage, TownRole requiredRole, String privilegeErrorCode, String... aliases) {
        super("town", subCommand, permissionNode, usage, aliases);
        this.description = MessageManager.getRawMessage("town_" + subCommand + "_subcommand_description");
        this.requiredRole = requiredRole;
        this.privilegeErrorCode = privilegeErrorCode;
    }

    @Override
    public void onCommand(Player player, String[] args) {
        if (requiredRole != null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> DataManager.canPerformTownCommand(player, requiredRole).thenAccept(canPerform -> {
                if (canPerform) {
                    super.onCommand(player, args);
                } else {
                    MessageManager.sendMessage(player, privilegeErrorCode != null ? privilegeErrorCode : "error_no_permission");
                }
            }));
        } else {
            super.onCommand(player, args);
        }
    }

}
