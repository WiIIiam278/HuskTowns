package me.william278.husktowns.command;

import de.themoep.minedown.MineDown;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class PlotCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        player.spigot().sendMessage(new MineDown("hiiiii <3").toComponent());
    }
}
