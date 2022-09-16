package net.william278.husktowns.command;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class CommandBase implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player) {
            onCommand((Player) sender, command, label, args);
            return true;
        }
        return false;
    }

    protected abstract void onCommand(Player player, Command command, String label, String[] args);

    /**
     * Register base for bukkit command
     *
     * @param command Command for registration
     */
    public PluginCommand register(PluginCommand command) {
        Objects.requireNonNull(command);
        command.setExecutor(this);
        if (this instanceof TabCompleter) {
            command.setTabCompleter((TabCompleter) this);
        }
        return command;
    }

    public static class EmptyTab implements TabCompleter {
        @Override
        public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
            return Collections.emptyList();
        }
    }

    public static class SimpleTab implements TabCompleter {
        public String[] commandTabArgs;

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String s, String[] args) {
            Player p = (Player) sender;
            if (command.getPermission() != null) {
                if (!p.hasPermission(command.getPermission())) {
                    return Collections.emptyList();
                }
            }
            if (args.length == 1) {
                final List<String> tabCompletions = new ArrayList<>();
                StringUtil.copyPartialMatches(args[0], Arrays.asList(commandTabArgs), tabCompletions);
                Collections.sort(tabCompletions);
                return tabCompletions;
            } else {
                return Collections.emptyList();
            }
        }
    }
}
