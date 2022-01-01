package me.william278.husktowns.data.message;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.chunk.ClaimedChunk;
import me.william278.husktowns.commands.InviteCommand;
import me.william278.husktowns.commands.TownChatCommand;
import me.william278.husktowns.data.message.pluginmessage.PluginMessage;
import me.william278.husktowns.data.message.redis.RedisMessage;
import me.william278.husktowns.town.TownBonus;
import me.william278.husktowns.town.TownInvite;
import me.william278.husktowns.town.TownRole;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.StringJoiner;
import java.util.UUID;
import java.util.logging.Level;

public class CrossServerMessageHandler {

    private static final HuskTowns plugin = HuskTowns.getInstance();

    // Move a player to a different server in the bungee network
    @SuppressWarnings("UnstableApiUsage")
    public static void movePlayerServer(Player p, String targetServer) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(targetServer);
            p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        });
    }

    public static void handleMessage(Message message, Player recipient) {
        switch (message.getMessageType()) {
            // Recipient not required for following messages
            case ADD_PLAYER_TO_CACHE -> {
                if (!HuskTowns.getPlayerCache().hasLoaded()) {
                    break;
                }
                final String[] playerDetails = message.getMessageDataItems();
                HuskTowns.getPlayerCache().setPlayerName(UUID.fromString(playerDetails[0]), playerDetails[1]);
            }
            case UPDATE_CACHED_GREETING_MESSAGE -> {
                if (!HuskTowns.getTownDataCache().hasLoaded()) {
                    break;
                }
                final String[] newGreetingDetails = message.getMessageDataItems();
                HuskTowns.getTownDataCache().setGreetingMessage(newGreetingDetails[0], newGreetingDetails[1]);
            }
            case UPDATE_CACHED_FAREWELL_MESSAGE -> {
                if (!HuskTowns.getTownDataCache().hasLoaded()) {
                    break;
                }
                final String[] newFarewellDetails = message.getMessageDataItems();
                HuskTowns.getTownDataCache().setFarewellMessage(newFarewellDetails[0], newFarewellDetails[1]);
            }
            case UPDATE_CACHED_BIO_MESSAGE -> {
                if (!HuskTowns.getTownDataCache().hasLoaded()) {
                    break;
                }
                final String[] newBioDetails = message.getMessageDataItems();
                HuskTowns.getTownDataCache().setTownBio(newBioDetails[0], newBioDetails[1]);
            }
            case TOWN_DISBAND -> {
                final String disbandingTown = message.getMessageData();
                HuskTowns.getClaimCache().removeAllClaims(disbandingTown);
                HuskTowns.getPlayerCache().disbandReload(disbandingTown);
                HuskTowns.getTownBonusesCache().clearTownBonuses(disbandingTown);
                HuskTowns.getTownDataCache().disbandReload(disbandingTown);
            }
            case TOWN_RENAME -> {
                final String[] renamingDetails = message.getMessageDataItems();
                final String oldName = renamingDetails[0];
                final String newName = renamingDetails[1];
                HuskTowns.getClaimCache().renameReload(oldName, newName);
                HuskTowns.getPlayerCache().renameReload(oldName, newName);
                HuskTowns.getTownBonusesCache().renameTown(oldName, newName);
                HuskTowns.getTownDataCache().renameReload(oldName, newName);
            }
            case TOWN_REMOVE_ALL_CLAIMS -> {
                final String unClaimingTown = message.getMessageData();
                if (HuskTowns.getClaimCache().hasLoaded()) {
                    HuskTowns.getClaimCache().removeAllClaims(unClaimingTown);
                }
            }
            case SET_PLAYER_TOWN -> {
                if (!HuskTowns.getPlayerCache().hasLoaded()) {
                    break;
                }
                final String[] newPlayerTownDetails = message.getMessageDataItems();
                final UUID playerToUpdate = UUID.fromString(newPlayerTownDetails[0]);
                final String playerTown = newPlayerTownDetails[1];
                HuskTowns.getPlayerCache().setPlayerTown(playerToUpdate, playerTown);
            }
            case CLEAR_PLAYER_TOWN -> {
                if (!HuskTowns.getPlayerCache().hasLoaded()) {
                    break;
                }
                final UUID townMemberToClear = UUID.fromString(message.getMessageData());
                HuskTowns.getPlayerCache().clearPlayerTown(townMemberToClear);
            }
            case SET_PLAYER_ROLE -> {
                if (!HuskTowns.getPlayerCache().hasLoaded()) {
                    break;
                }
                final String[] newPlayerRoleDetails = message.getMessageDataItems();
                final UUID playerRoleToUpdate = UUID.fromString(newPlayerRoleDetails[0]);
                final TownRole role = TownRole.valueOf(newPlayerRoleDetails[1]);
                HuskTowns.getPlayerCache().setPlayerRole(playerRoleToUpdate, role);
            }
            case CLEAR_PLAYER_ROLE -> {
                if (!HuskTowns.getPlayerCache().hasLoaded()) {
                    break;
                }
                final UUID roleHolderToClear = UUID.fromString(message.getMessageData());
                HuskTowns.getPlayerCache().clearPlayerRole(roleHolderToClear);
            }
            case TOWN_CHAT_MESSAGE -> {
                final String[] messageData = message.getMessageDataItems();
                final String townChatMessage = messageData[2].replaceAll("💲", "$");
                Bukkit.getScheduler().runTaskAsynchronously(HuskTowns.getInstance(), () -> TownChatCommand.dispatchTownMessage(messageData[0], messageData[1], townChatMessage));
            }
            case ADD_TOWN_BONUS -> {
                final String[] bonusToAddData = message.getMessageDataItems();
                final String townName = bonusToAddData[0];
                final int bonusClaims = Integer.parseInt(bonusToAddData[1]);
                final int bonusMembers = Integer.parseInt(bonusToAddData[2]);
                final long appliedTimestamp = Long.parseLong(bonusToAddData[3]);
                if (bonusToAddData.length == 5) {
                    final UUID applier = UUID.fromString(bonusToAddData[4]);
                    HuskTowns.getTownBonusesCache().add(townName, new TownBonus(applier, bonusClaims, bonusMembers, appliedTimestamp));
                    break;
                }
                HuskTowns.getTownBonusesCache().add(townName, new TownBonus(null, bonusClaims, bonusMembers, appliedTimestamp));
            }
            case CLEAR_TOWN_BONUSES -> HuskTowns.getTownBonusesCache().clearTownBonuses(message.getMessageData());
            case SET_TOWN_SPAWN_PRIVACY -> {
                final String[] townSpawnPrivacyUpdateData = message.getMessageDataItems();
                final String townNameToUpdatePrivacyOf = townSpawnPrivacyUpdateData[0];
                final boolean isTownSpawnPublic = Boolean.parseBoolean(townSpawnPrivacyUpdateData[1]);
                if (isTownSpawnPublic) {
                    HuskTowns.getTownDataCache().addTownWithPublicSpawn(townNameToUpdatePrivacyOf);
                } else {
                    HuskTowns.getTownDataCache().removeTownWithPublicSpawn(townNameToUpdatePrivacyOf);
                }
            }
            case UPDATE_TOWN_FLAG -> {
                final String[] townFlagUpdateItems = message.getMessageDataItems();
                final String townName = townFlagUpdateItems[0];
                final ClaimedChunk.ChunkType chunkType = ClaimedChunk.ChunkType.valueOf(townFlagUpdateItems[1].toUpperCase());
                final String flagIdentifier = townFlagUpdateItems[2];
                final boolean flagValue = Boolean.parseBoolean(townFlagUpdateItems[3]);
                HuskTowns.getTownDataCache().setFlag(townName, chunkType, flagIdentifier, flagValue);
            }
            case CREATE_TOWN_FLAGS -> {
                final String townName = message.getMessageData();
                if (townName.equalsIgnoreCase(HuskTowns.getSettings().getAdminTownName())) {
                    HuskTowns.getTownDataCache().setFlags(townName, HuskTowns.getSettings().getAdminClaimFlags());
                } else {
                    HuskTowns.getTownDataCache().setFlags(townName, HuskTowns.getSettings().getDefaultClaimFlags());
                }
            }
            default -> {
                // Recipient is required for following messages
                if (recipient == null) {
                    return;
                }

                // Check recipient-required messages
                switch (message.getMessageType()) {
                    case DEMOTED_NOTIFICATION -> {
                        final String[] demotionDetails = message.getMessageDataItems();
                        MessageManager.sendMessage(recipient, "player_demoted",
                                demotionDetails[0], demotionDetails[1], demotionDetails[2]);
                    }
                    case PROMOTED_NOTIFICATION -> {
                        final String[] promotionDetails = message.getMessageDataItems();
                        MessageManager.sendMessage(recipient, "player_promoted",
                                promotionDetails[0], promotionDetails[1], promotionDetails[2]);
                    }
                    case EVICTED_NOTIFICATION -> {
                        final String[] evictionDetails = message.getMessageDataItems();
                        MessageManager.sendMessage(recipient, "player_evicted",
                                evictionDetails[0], evictionDetails[1]);
                    }
                    case DEMOTED_NOTIFICATION_YOURSELF -> {
                        final String[] demotedDetails = message.getMessageDataItems();
                        MessageManager.sendMessage(recipient, "have_been_demoted",
                                demotedDetails[0], demotedDetails[1]);
                    }
                    case PROMOTED_NOTIFICATION_YOURSELF -> {
                        final String[] promotedDetails = message.getMessageDataItems();
                        MessageManager.sendMessage(recipient, "have_been_promoted",
                                promotedDetails[0], promotedDetails[1]);
                    }
                    case EVICTED_NOTIFICATION_YOURSELF -> {
                        final String[] evictedDetails = message.getMessageDataItems();
                        MessageManager.sendMessage(recipient, "have_been_evicted",
                                evictedDetails[0], evictedDetails[1]);
                    }
                    case TRANSFER_YOU_NOTIFICATION -> {
                        final String[] transferredDetails = message.getMessageDataItems();
                        MessageManager.sendMessage(recipient, "town_transferred_to_you", transferredDetails[0], transferredDetails[1]);
                    }
                    case DEPOSIT_NOTIFICATION -> {
                        final String[] depositDetails = message.getMessageDataItems();
                        MessageManager.sendMessage(recipient, "town_deposit_notification", depositDetails[0], depositDetails[1]);
                    }
                    case LEVEL_UP_NOTIFICATION -> {
                        final String[] levelUpDetails = message.getMessageDataItems();
                        MessageManager.sendMessage(recipient, "town_level_up_notification", levelUpDetails[0], levelUpDetails[1], levelUpDetails[2]);
                    }
                    case INVITED_TO_JOIN -> {
                        final String[] inviteDetails = message.getMessageDataItems();
                        InviteCommand.sendInvite(recipient, new TownInvite(inviteDetails[0],
                                inviteDetails[1], Long.parseLong(inviteDetails[2])));
                    }
                    case INVITED_TO_JOIN_REPLY -> {
                        String[] replyDetails = message.getMessageDataItems();
                        boolean accepted = Boolean.parseBoolean(replyDetails[0]);
                        if (accepted) {
                            MessageManager.sendMessage(recipient, "invite_accepted", replyDetails[1], replyDetails[2]);
                        } else {
                            MessageManager.sendMessage(recipient, "invite_rejected", replyDetails[1], replyDetails[2]);
                        }
                    }
                    case INVITED_NOTIFICATION -> {
                        final String[] invitedDetails = message.getMessageDataItems();
                        MessageManager.sendMessage(recipient, "player_invited",
                                invitedDetails[0], invitedDetails[1]);
                    }
                    case DISBAND_NOTIFICATION -> {
                        final String[] disbandedDetails = message.getMessageDataItems();
                        MessageManager.sendMessage(recipient, "town_disbanded", disbandedDetails[0], disbandedDetails[1]);
                    }
                    case RENAME_NOTIFICATION -> {
                        final String[] renameDetails = message.getMessageDataItems();
                        MessageManager.sendMessage(recipient, "town_renamed", renameDetails[0], renameDetails[1]);
                    }
                    case TRANSFER_NOTIFICATION -> {
                        final String[] transferDetails = message.getMessageDataItems();
                        MessageManager.sendMessage(recipient, "town_transferred", transferDetails[0], transferDetails[1], transferDetails[2]);
                    }
                    case PLAYER_HAS_JOINED_NOTIFICATION -> {
                        final String playerName = message.getMessageData();
                        MessageManager.sendMessage(recipient, "player_joined", playerName);
                    }
                    case REMOVE_ALL_CLAIMS_NOTIFICATION -> {
                        final String mayorName = message.getMessageData();
                        MessageManager.sendMessage(recipient, "town_unclaim_all_notification", mayorName);
                    }
                    case GET_PLAYER_LIST -> {
                        final String requestingServer = message.getMessageData();
                        StringJoiner playerList = new StringJoiner("£");
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            playerList.add(p.getName());
                        }
                        if (playerList.toString().equals("")) {
                            return;
                        }
                        CrossServerMessageHandler.getMessage(Message.MessageType.RETURN_PLAYER_LIST, playerList.toString()).sendToServer(recipient, requestingServer);
                    }
                    case RETURN_PLAYER_LIST -> {
                        final String[] returningPlayers = message.getMessageData().split("£");
                        HuskTowns.getPlayerList().addPlayers(returningPlayers);
                    }
                    default -> HuskTowns.getInstance().getLogger().log(Level.WARNING, "Received a HuskTowns plugin message with an unrecognised type. Is your version of HuskTowns up to date?");
                }
            }
        }
    }

    public static Message getMessage(String targetPlayerName, Message.MessageType pluginMessageType, String... messageData) {
        return switch (HuskTowns.getSettings().getMessengerType()) {
            case PLUGIN_MESSAGE -> new PluginMessage(targetPlayerName, pluginMessageType, messageData);
            case REDIS -> new RedisMessage(targetPlayerName, pluginMessageType, messageData);
        };
    }

    public static Message getMessage(Message.MessageType pluginMessageType, String... messageData) {
        return switch (HuskTowns.getSettings().getMessengerType()) {
            case PLUGIN_MESSAGE -> new PluginMessage(pluginMessageType, messageData);
            case REDIS -> new RedisMessage(pluginMessageType, messageData);
        };
    }
}
