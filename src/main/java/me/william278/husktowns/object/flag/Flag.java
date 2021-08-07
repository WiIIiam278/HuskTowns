package me.william278.husktowns.object.flag;

import de.themoep.minedown.MineDown;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.listener.EventListener;
import me.william278.husktowns.object.cache.ClaimCache;
import me.william278.husktowns.object.cache.TownDataCache;
import me.william278.husktowns.object.chunk.ClaimedChunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public abstract class Flag {

    private final String identifier; // Identifier must match SQL column name
    private final String displayName;
    private final String description;
    private final HashSet<EventListener.ActionType> matchingActions = new HashSet<>();
    private boolean allowed;

    public Flag(String flagName, String displayName, String flagDescription, boolean allowed, EventListener.ActionType... matchingActions) {
        this.identifier = flagName;
        this.displayName = displayName;
        this.description = flagDescription;
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

    public boolean isFlagSet() {
        return allowed;
    }

    public void setFlag(boolean allowed) {
        this.allowed = allowed;
    }

    public boolean actionMatches(EventListener.ActionType actionType) {
        return matchingActions.contains(actionType);
    }

    public boolean isActionAllowed(EventListener.ActionType actionType) {
        if (matchingActions.contains(actionType)) {
            return isFlagSet();
        }
        return true;
    }

    private static boolean doFlagsPermitAction(EventListener.ActionType type, HashSet<Flag> flags) {
        for (Flag flag : flags) {
            if (!flag.isActionAllowed(type)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isActionAllowed(Location location, EventListener.ActionType actionType) {
        World world = location.getWorld();
        if (world == null) {
            return false;
        }
        final String worldName = world.getName();
        if (HuskTowns.getSettings().getUnClaimableWorlds().contains(worldName)) {
            // Check against un-claimable world flags
            return doFlagsPermitAction(actionType, HuskTowns.getSettings().getUnClaimableWorldFlags());
        } else {
            final ClaimCache claimCache = HuskTowns.getClaimCache();
            if (!claimCache.hasLoaded()) {
                return false;
            }
            final ClaimedChunk chunk = claimCache.getChunkAt(location.getChunk().getX(), location.getChunk().getZ(), worldName);
            if (chunk == null) {
                // Check against wilderness flags
                return doFlagsPermitAction(actionType, HuskTowns.getSettings().getWildernessFlags());
            } else {
                // Check against the town's flags
                final TownDataCache townDataCache = HuskTowns.getTownDataCache();
                if (!townDataCache.hasLoaded()) {
                    return false;
                }
                return doFlagsPermitAction(actionType, townDataCache.getFlags(chunk.getTown(), chunk.getChunkType()));
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

            builder.append(MessageManager.getRawMessage("settings_menu_flag_item", flagStrings.get(ClaimedChunk.ChunkType.REGULAR), flagStrings.get(ClaimedChunk.ChunkType.FARM), flagStrings.get(ClaimedChunk.ChunkType.PLOT), MineDown.escape(flagName), MineDown.escape(flagDescription)));
            builder.append("\n");
        }
        return builder.toString();
    }

}