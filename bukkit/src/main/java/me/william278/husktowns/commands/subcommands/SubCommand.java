package me.william278.husktowns.commands.subcommands;

import me.william278.husktowns.MessageManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class SubCommand {

    public final String parentCommand;
    public final String subCommand;
    public final String permissionNode;
    private final String usage;
    public String[] aliases;

    public String getUsage() {
        return "/" + parentCommand + " " + subCommand + " " + usage;
    }

    public SubCommand(String parentCommand, String subCommand, String permissionNode, String usage, String... aliases) {
        this.parentCommand = parentCommand;
        this.subCommand = subCommand;
        this.permissionNode = permissionNode;
        this.usage = usage;
        this.aliases = aliases;
    }

    // Execute the command if the player has the permission node
    public void onCommand(Player player, String[] args) {
        if (permissionNode != null) {
            if (!player.hasPermission(permissionNode)) {
                MessageManager.sendMessage(player, "error_no_permission");
                return;
            }
        }

        // Remove first element of args array
        ArrayList<String> arguments = new ArrayList<>(List.of(args));
        arguments.remove(0);
        String[] newArgs = new String[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            newArgs[i] = arguments.get(i);
        }

        // Execute the subcommand
        onExecute(player, newArgs);
    }

    public abstract void onExecute(Player player, String[] args);

}
