package net.william278.husktowns.command;

import me.lucko.commodore.CommodoreProvider;
import me.lucko.commodore.file.CommodoreFileReader;
import net.william278.husktowns.BukkitHuskTowns;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

/**
 * Used for registering Brigadier hooks on platforms that support commodore for rich command syntax
 */
public class BrigadierUtil {

    protected static void registerCommodore(@NotNull BukkitHuskTowns plugin, @NotNull PluginCommand pluginCommand,
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