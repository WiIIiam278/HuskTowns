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
import me.lucko.commodore.file.CommodoreFileReader;
import net.william278.husktowns.BukkitHuskTowns;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

/**
 * Used for registering Brigadier hooks on platforms that support commodore for rich command syntax
 */
public class BrigadierUtil {

    protected static void registerCommodore(@NotNull BukkitHuskTowns plugin, @NotNull org.bukkit.command.Command pluginCommand,
                                            @NotNull Command command) {
        // Register command descriptions via commodore (brigadier wrapper)
        try (InputStream commandFile = plugin.getResource("commodore/" + command.getName() + ".commodore")) {
            CommodoreProvider.getCommodore(plugin).register(pluginCommand, CommodoreFileReader.INSTANCE.parse(commandFile),
                    player -> player.hasPermission(command.getPermission()));
        } catch (IOException e) {
            plugin.log(Level.SEVERE, "Failed to load " + command.getName() + ".commodore command definitions", e);
        }
    }

}