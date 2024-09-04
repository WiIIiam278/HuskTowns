/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.husktowns.command;

import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.user.BukkitUser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BukkitCommand extends org.bukkit.command.Command {

    private final BukkitHuskTowns plugin;
    private final Command command;

    public BukkitCommand(@NotNull Command command, @NotNull BukkitHuskTowns plugin) {
        super(command.getName(), command.getUsage(), command.getUsage(), command.getAliases());
        this.command = command;
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        this.command.execute(
            sender instanceof Player player ? BukkitUser.adapt(player, plugin) : plugin.getConsole(), args
        );
        return true;
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias,
                                    @NotNull String[] args) throws IllegalArgumentException {
        return this.command.getSuggestions(
            sender instanceof Player player ? BukkitUser.adapt(player, plugin) : plugin.getConsole(), args
        );
    }

    public void register() {
        // Register with bukkit
        plugin.getCommandRegistrar().getServerCommandMap().register("husktowns", this);

        // Register permissions
        this.registerPermissions(command, plugin);
    }

    private void registerPermissions(@NotNull Command command, @NotNull BukkitHuskTowns plugin) {
        // Register permissions
        final PluginManager manager = plugin.getServer().getPluginManager();
        command.getChildren()
            .stream().map(child -> new Permission(child.getPermission(), child.getUsage(),
                child.isOperatorCommand() ? PermissionDefault.OP : PermissionDefault.TRUE))
            .forEach(manager::addPermission);
        manager.addPermission(new Permission(command.getPermission(), "/" + command.getName(),
            command.isOperatorCommand() ? PermissionDefault.OP : PermissionDefault.TRUE));

        // Register primary permission
        final Map<String, Boolean> childNodes = new HashMap<>();
        command.getChildren().forEach(child -> childNodes.put(child.getPermission(), true));
        manager.addPermission(new Permission(command.getPermission() + ".*", command.getUsage(),
            PermissionDefault.FALSE, childNodes));
    }
}
