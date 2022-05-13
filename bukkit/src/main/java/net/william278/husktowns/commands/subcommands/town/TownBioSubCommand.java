package net.william278.husktowns.commands.subcommands.town;

import net.william278.husktowns.MessageManager;
import net.william278.husktowns.commands.subcommands.TownSubCommand;
import net.william278.husktowns.data.DataManager;
import net.william278.husktowns.town.TownRole;
import org.bukkit.entity.Player;

import java.util.StringJoiner;

public class TownBioSubCommand extends TownSubCommand {

    public TownBioSubCommand() {
        super("bio", "husktowns.command.town.message.bio", "<bio>",
                TownRole.getLowestRoleWithPermission(TownRole.RolePrivilege.BIO), "error_insufficient_bio_privileges");
    }

    @Override
    public void onExecute(Player player, String[] args) {
        if (args.length >= 1) {
            StringJoiner description = new StringJoiner(" ");
            for (String arg : args) {
                description.add(arg);
            }

            DataManager.updateTownBio(player, description.toString().replaceAll("&k", "&r"));
        } else {
            MessageManager.sendMessage(player, "error_invalid_syntax", getUsage());
        }
    }
}
