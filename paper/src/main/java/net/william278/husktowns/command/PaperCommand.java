/*
 * This file is part of HuskTowns by William278. Do not redistribute!
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  All rights reserved.
 *
 *  This source code is provided as reference to licensed individuals that have purchased the HuskTowns
 *  plugin once from any of the official sources it is provided. The availability of this code does
 *  not grant you the rights to modify, re-distribute, compile or redistribute this source code or
 *  "plugin" outside this intended purpose. This license does not cover libraries developed by third
 *  parties that are utilised in the plugin.
 */

package net.william278.husktowns.command;

import me.lucko.commodore.CommodoreProvider;
import net.william278.husktowns.PaperHuskTowns;
import net.william278.husktowns.user.BukkitUser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PaperCommand extends org.bukkit.command.Command {

    private final PaperHuskTowns plugin;
    private final Command command;

    public PaperCommand(@NotNull Command command, @NotNull PaperHuskTowns plugin) {
        super(command.getName(), command.getUsage(), command.getUsage(), command.getAliases());
        this.command = command;
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        this.command.execute(sender instanceof Player player ? BukkitUser.adapt(player) : plugin.getConsole(), args);
        return true;
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return this.command.getSuggestions(sender instanceof Player player ? BukkitUser.adapt(player) : plugin.getConsole(), args);
    }

    public void register() {
        // Register with bukkit
        plugin.getServer().getCommandMap().register("husktowns", this);

        // Register permissions
        BukkitCommand.registerPermissions(command, plugin);

        // Register commodore TAB completion
        if (CommodoreProvider.isSupported() && plugin.getSettings().doBrigadierTabCompletion()) {
            BrigadierUtil.registerCommodore(plugin, this, command);
        }
    }

}
