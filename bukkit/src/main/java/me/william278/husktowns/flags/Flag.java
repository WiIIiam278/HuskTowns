package me.william278.husktowns.flags;

import de.themoep.minedown.MineDown;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.listener.ActionType;
import me.william278.husktowns.cache.ClaimCache;
import me.william278.husktowns.cache.TownDataCache;
import me.william278.husktowns.chunk.ClaimedChunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public abstract class Flag {

    private final String identifier; // Identifier must match SQL column name
    private final String displayName;
    private final String description;
    private final HashSet<ActionType> matchingActions = new HashSet<>();
    private boolean allowed;

    public Flag(String flagName, boolean allowed, ActionType... matchingActions) {
        this.identifier = flagName;
        this.displayName = MessageManager.getRawMessage("flag_" + identifier);
        this.description = MessageManager.getRawMessage("flag_" + identifier + "_description");
        this.allowed = allowed;
        this.matchingActions.addAll(Arrays.asList(matchingActions));
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getSetPermission() {
        return "husktowns.command.town.flag.set." + identifier;
    }

    public boolean isFlagSet() {
        return allowed;
    }

    public void setFlag(boolean allowed) {
        this.allowed = allowed;
    }

    public boolean actionMatches(ActionType actionType) {
        return matchingActions.contains(actionType);
    }

    public boolean isActionAllowed(ActionType actionType) {
        if (matchingActions.contains(actionType)) {
            return isFlagSet();
        }
        return true;
    }

    private static boolean doFlagsPermitAction(ActionType type, HashSet<Flag> flags) {
        for (Flag flag : flags) {
            if (flag.actionMatches(type)) {
                return flag.isActionAllowed(type);
            }
        }
        return true;
    }

    public static boolean isActionAllowed(Location location, ActionType actionType) {
        World world = location.getWorld();
        if (world == null) {
            MessageManager.sendVerboseMessage("&7" + actionType.toString() + " allowed is true &8(world is null)");
            return false;
        }
        final String worldName = world.getName();
        if (HuskTowns.getSettings().getUnClaimableWorlds().contains(worldName)) {
            // Check against un-claimable world flags
            final boolean doFlagsPermitAction = doFlagsPermitAction(actionType, HuskTowns.getSettings().getUnClaimableWorldFlags());
            MessageManager.sendVerboseMessage("&7" + actionType.toString() + " allowed is " + doFlagsPermitAction + " &8(in un-claimable world)");
            return doFlagsPermitAction;
        } else {
            final ClaimCache claimCache = HuskTowns.getClaimCache();
            if (!claimCache.hasLoaded()) {
                MessageManager.sendVerboseMessage("&7" + actionType.toString() + " allowed is false &8(claim cache not loaded)");
                return false;
            }
            final ClaimedChunk chunk = claimCache.getChunkAt(location.getChunk().getX(), location.getChunk().getZ(), worldName);
            if (chunk == null) {
                // Check against wilderness flags
                final boolean doFlagsPermitAction = doFlagsPermitAction(actionType, HuskTowns.getSettings().getWildernessFlags());
                MessageManager.sendVerboseMessage("&7" + actionType.toString() + " allowed is " + doFlagsPermitAction + " &8(in wilderness)");
                return doFlagsPermitAction;
            } else {
                // Check against the town's flags
                final TownDataCache townDataCache = HuskTowns.getTownDataCache();
                if (!townDataCache.hasLoaded()) {
                    return false;
                }
                final boolean doFlagsPermitAction = doFlagsPermitAction(actionType, townDataCache.getFlags(chunk.getTown(), chunk.getChunkType()));
                MessageManager.sendVerboseMessage("&7" + actionType.toString() + " allowed is " + doFlagsPermitAction + " &8(in " + chunk.getTown() + "'s " + chunk.getChunkType().toString() + " claim)");
                return doFlagsPermitAction;
            }
        }
    }

    public static String getTownFlagMenu(HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> flags, String townName, boolean canPlayerEdit) {
        StringBuilder builder = new StringBuilder();
        if (canPlayerEdit) {
            builder.append(MessageManager.getRawMessage("settings_menu_flag_subheading"));
        } else {
            builder.append(MessageManager.getRawMessage("settings_menu_flag_subheading_static"));
        }
        if (townName.equalsIgnoreCase(HuskTowns.getSettings().getAdminTownName())) {
            builder.append(MessageManager.getRawMessage("settings_menu_admin_flags"))
                    .append("\n");
        }
        SortedMap<String, HashMap<ClaimedChunk.ChunkType, Flag>> flagTypes = new TreeMap<>();

        for (ClaimedChunk.ChunkType type : flags.keySet()) {
            for (Flag flag : flags.get(type)) {
                if (!flagTypes.containsKey(flag.getIdentifier())) {
                    flagTypes.put(flag.getIdentifier(), new HashMap<>());
                }
                flagTypes.get(flag.getIdentifier()).put(type, flag);
            }
        }

        for (String flagID : flagTypes.keySet()) {
            final String flagName = flagTypes.get(flagID).get(ClaimedChunk.ChunkType.REGULAR).getDisplayName();
            final String flagDescription = flagTypes.get(flagID).get(ClaimedChunk.ChunkType.REGULAR).getDescription();
            final HashMap<ClaimedChunk.ChunkType, String> flagStrings = new HashMap<>();
            for (ClaimedChunk.ChunkType type : flagTypes.get(flagID).keySet()) {
                final Flag flag = flagTypes.get(flagID).get(type);
                final boolean flagSet = flag.isFlagSet();
                if (flagSet) {
                    if (canPlayerEdit) {
                        flagStrings.put(type, MessageManager.getRawMessage("settings_menu_flag_set", MineDown.escape(flagName), type.name().toLowerCase(), MineDown.escape(flagID), townName));
                    } else {
                        flagStrings.put(type, MessageManager.getRawMessage("settings_menu_flag_set_static", MineDown.escape(flagName), type.name().toLowerCase(), MineDown.escape(flagID), townName));
                    }
                } else {
                    if (canPlayerEdit) {
                        flagStrings.put(type, MessageManager.getRawMessage("settings_menu_flag_unset", MineDown.escape(flagName), type.name().toLowerCase(), MineDown.escape(flagID), townName));
                    } else {
                        flagStrings.put(type, MessageManager.getRawMessage("settings_menu_flag_unset_static", MineDown.escape(flagName), type.name().toLowerCase(), MineDown.escape(flagID), townName));
                    }
                }
            }
            builder.append(MessageManager.getRawMessage("settings_menu_flag_item", flagStrings.get(ClaimedChunk.ChunkType.REGULAR), flagStrings.get(ClaimedChunk.ChunkType.FARM), flagStrings.get(ClaimedChunk.ChunkType.PLOT), MineDown.escape(flagName), MineDown.escape(flagDescription)))
                    .append("\n");
        }
        return builder.toString();
    }

}