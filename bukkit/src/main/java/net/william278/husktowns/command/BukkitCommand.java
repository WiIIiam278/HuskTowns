package net.william278.husktowns.command;

import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.user.BukkitUser;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class BukkitCommand implements CommandExecutor, TabCompleter {

    private final BukkitHuskTowns plugin;
    private final Command command;

    public BukkitCommand(@NotNull Command command, @NotNull BukkitHuskTowns plugin) {
        this.command = command;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command,
                             @NotNull String label, @NotNull String[] args) {
        this.command.execute(sender instanceof Player player ? BukkitUser.adapt(player) : plugin.getConsole(), args);
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        return this.command.suggest(sender instanceof Player player ? BukkitUser.adapt(player) : plugin.getConsole(), args);
    }

    public void register() {
        final PluginCommand pluginCommand = Objects.requireNonNull(plugin.getCommand(command.getName()));
        pluginCommand.setExecutor(this);
        pluginCommand.setTabCompleter(this);
    }
}
