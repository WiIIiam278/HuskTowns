package me.william278.husktowns.command;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.data.pluginmessage.PluginMessage;
import me.william278.husktowns.data.pluginmessage.PluginMessageType;
import me.william278.husktowns.object.town.TownInvite;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.Locale;

public class InviteCommand extends CommandBase {

    private static void handleInviteAccepting(Player player, boolean accepted) {
        if (!HuskTowns.invites.containsKey(player.getUniqueId())) {
            MessageManager.sendMessage(player, "no_pending_invite");
            return;
        }
        TownInvite invite = HuskTowns.invites.get(player.getUniqueId());
        if (invite.hasExpired()) {
            MessageManager.sendMessage(player, "error_invite_expired");
            HuskTowns.invites.remove(player.getUniqueId());
            return;
        }

        Player inviter = Bukkit.getPlayer(invite.getInviter());
        if (inviter != null) {
            if (accepted) {
                MessageManager.sendMessage(inviter, "invite_accepted", player.getName(), invite.getTownName());
            } else {
                MessageManager.sendMessage(inviter, "invite_rejected", player.getName(), invite.getTownName());
            }
        } else {
            new PluginMessage(HuskTowns.getPlayerCache().getUsername(invite.getInviter()), PluginMessageType.INVITED_TO_JOIN_REPLY, accepted + "$" + player.getName() + "$" + invite.getTownName()).send(player);
        }

        if (!accepted) {
            MessageManager.sendMessage(player, "have_invite_rejected",
                    HuskTowns.getPlayerCache().getUsername(invite.getInviter()), invite.getTownName());
            HuskTowns.invites.remove(player.getUniqueId());
            return;
        }
        HuskTowns.invites.remove(player.getUniqueId());
        DataManager.joinTown(player, invite.getTownName());
    }

    public static void sendInviteCrossServer(Player sender, String recipientName, TownInvite townInvite) {
        new PluginMessage(recipientName, PluginMessageType.INVITED_TO_JOIN,
                townInvite.getTownName() + "$" + townInvite.getInviter() + "$" + townInvite.getExpiry()).send(sender);
    }

    public static void sendInvite(Player recipient, TownInvite townInvite) {
        HuskTowns.invites.put(recipient.getUniqueId(), townInvite);
        MessageManager.sendMessage(recipient, "invite_received",
                townInvite.getTownName(), HuskTowns.getPlayerCache().getUsername(townInvite.getInviter()));
    }

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (args.length == 1) {
            String targetPlayer = args[0];
            switch (targetPlayer.toLowerCase(Locale.ENGLISH)) {
                case "accept":
                case "yes":
                    handleInviteAccepting(player, true);
                    return;
                case "reject":
                case "deny":
                case "decline":
                case "no":
                    handleInviteAccepting(player, false);
                    return;
            }
            DataManager.sendInvite(player, targetPlayer);
        } else {
            MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
        }
    }
}
