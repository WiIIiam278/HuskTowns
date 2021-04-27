package me.william278.husktowns.command;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.object.PageChatList;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class HuskTownsCommand extends CommandBase {

    private void showHelpMenu(Player player, int pageNumber) {
        ArrayList<String> commandDisplay = new ArrayList<>();
        for (String cmd : HuskTowns.getCommandDetails().keySet()) {
            commandDisplay.add("[/" + cmd + "](#4af7c9 show_message=&7Click to suggest suggest_command=/"  + cmd + ") [•](white) [" + HuskTowns.getCommandDetails().get(cmd) + "](gray)");
        }
        MessageManager.sendMessage(player, "command_list_header");
        PageChatList helpList = new PageChatList(commandDisplay, 10, "/husktowns help");
        if (!helpList.hasPage(pageNumber)) {
            MessageManager.sendMessage(player, "error_invalid_page_number");
            return;
        }
        player.spigot().sendMessage(helpList.getPage(pageNumber));
    }

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (args.length >= 1) {
            switch (args[0]) {
                case "help":
                    if (args.length == 2) {
                        try {
                            int pageNo = Integer.parseInt(args[1]);
                            showHelpMenu(player, pageNo);
                        } catch (NumberFormatException ex) {
                            MessageManager.sendMessage(player, "error_invalid_page_number");
                        }
                    } else {
                        showHelpMenu(player, 1);
                    }
                    //todo help menu
                    break;
                case "about":
                case "info":
                    //todo other stuff
                    break;
                case "update":
                    //todo version checker
                    break;
            }
        } else {
            showHelpMenu(player, 1);
        }
    }
}
