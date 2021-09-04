package me.william278.husktowns.commands;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.data.pluginmessage.PluginMessage;
import me.william278.husktowns.town.TownInvite;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class InviteCommand extends CommandBase {

    private static void handleInviteAccepting(Player player, boolean accepted) {
        if (!HuskTowns.invites.containsKey(player.getUniqueId())) {
            MessageManager.sendMessage(player, "no_pending_invite");
            return;
        }
        final TownInvite invite = HuskTowns.invites.get(player.getUniqueId());
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
            if (HuskTowns.getSettings().doBungee()) {
                new PluginMessage(invite.getInviter(), PluginMessage.PluginMessageType.INVITED_TO_JOIN_REPLY, accepted + "$" + player.getName() + "$" + invite.getTownName()).send(player);
            }
        }

        HuskTowns.invites.remove(player.getUniqueId());

        if (accepted) {
            DataManager.joinTown(player, invite.getTownName());
        } else {
            MessageManager.sendMessage(player, "have_invite_rejected",
                    invite.getInviter(), invite.getTownName());
        }
    }

    public static void sendInvite(Player recipient, TownInvite townInvite) {
        HuskTowns.invites.put(recipient.getUniqueId(), townInvite);
        MessageManager.sendMessage(recipient, "invite_received",
                townInvite.getTownName(), townInvite.getInviter());
    }

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (args.length == 1) {
            String targetPlayer = args[0];
            switch (targetPlayer.toLowerCase()) {
                case "accept", "yes" -> {
                    handleInviteAccepting(player, true);
                    return;
                }
                case "reject", "deny", "decline", "no" -> {
                    handleInviteAccepting(player, false);
                    return;
                }
            }
            DataManager.sendInvite(player, targetPlayer);
        } else {
            MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
        }
    }
}
