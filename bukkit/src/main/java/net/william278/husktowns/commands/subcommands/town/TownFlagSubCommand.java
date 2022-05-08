package net.william278.husktowns.commands.subcommands.town;

import net.william278.husktowns.MessageManager;
import net.william278.husktowns.chunk.ClaimedChunk;
import net.william278.husktowns.commands.subcommands.TownSubCommand;
import net.william278.husktowns.data.DataManager;
import net.william278.husktowns.town.TownRole;
import org.bukkit.entity.Player;

public class TownFlagSubCommand extends TownSubCommand {

    public TownFlagSubCommand() {
        super("flag", "husktowns.command.town.flag", "[town] <chunk_type> <flag> <value>", TownRole.TRUSTED, "error_insufficient_flag_privileges");
    }

    @Override
    public void onExecute(Player player, String[] args) {
        String townName = null;
        ClaimedChunk.ChunkType chunkType;
        String flagIdentifier;
        boolean flagValue;
        boolean showSettingsMenu = false;

        // Check if the flag is being set via -settingsMenu
        if (args.length == 5) {
            showSettingsMenu = (args[4].equals("-settings"));
        }

        // Check for town name
        int argIndexer = 1;
        if (args.length == 4 || args.length == 5) {
            townName = args[argIndexer];
            argIndexer++;
        }

        // Check chunk type and flag data
        if (args.length >= 4 && args.length <= 5) {
            try {
                chunkType = ClaimedChunk.ChunkType.valueOf(args[argIndexer].toUpperCase());
                argIndexer++;
                flagIdentifier = args[argIndexer];
                argIndexer++;
                flagValue = Boolean.parseBoolean(args[argIndexer]);
                if (townName == null) {
                    DataManager.setTownFlag(player, chunkType, flagIdentifier, flagValue, showSettingsMenu);
                } else {
                    DataManager.setTownFlag(player, townName, chunkType, flagIdentifier, flagValue, showSettingsMenu);
                }
            } catch (IllegalArgumentException e) {
                MessageManager.sendMessage(player, "error_invalid_chunk_type");
            }
        } else {
            MessageManager.sendMessage(player, "error_invalid_syntax", getUsage());
        }
    }
}
