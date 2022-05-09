package net.william278.husktowns.town;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.MessageManager;
import net.william278.husktowns.chunk.ClaimedChunk;
import net.william278.husktowns.flags.Flag;
import net.william278.husktowns.teleport.TeleportationPoint;
import net.william278.husktowns.util.TownLimitsUtil;
import org.bukkit.entity.Player;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

public class Town {

    // Set of all chunks claimed by the town, including PlotChunks and FarmChunks
    private final HashSet<ClaimedChunk> claimedChunks = new HashSet<>();

    // TeleportationPoint of the town's spawn position
    private final TeleportationPoint townSpawn;

    // Whether the town spawn can be accessed by people not in the town
    private final boolean spawnPublic;

    // HashSet of all town members
    private final HashMap<UUID, TownRole> members = new HashMap<>();

    // Name of the town
    private final String name;

    // Amount of money deposited into town
    private final double moneyDeposited;

    // Greeting and farewell messages
    private final String greetingMessage;
    private final String farewellMessage;

    // Town bio
    private final String bio;

    // Timestamp when the town was founded
    private final long foundedTimestamp;

    // The town's flag settings for regular chunks, farms & plots
    private final HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> flags;

    /**
     * Create an Admin town to represent Administrators' claims
     */
    public Town() {
        this.townSpawn = null;
        this.spawnPublic = false;
        this.moneyDeposited = 0D;
        this.name = HuskTowns.getSettings().getAdminTownName();
        this.greetingMessage = MessageManager.getRawMessage("admin_claim_greeting_message", name);
        this.farewellMessage = MessageManager.getRawMessage("admin_claim_farewell_message", name);
        this.bio = MessageManager.getRawMessage("admin_town_bio");
        this.foundedTimestamp = Instant.now().getEpochSecond();
        this.flags = HuskTowns.getSettings().getAdminClaimFlags();
    }

    /**
     * Create a new Town at the Mayor's position
     *
     * @param mayor the Player who will be the mayor of the town
     */
    public Town(Player mayor, String name) {
        this.townSpawn = null;
        this.spawnPublic = false;
        this.moneyDeposited = 0D;
        this.name = name;
        this.claimedChunks.add(new ClaimedChunk(mayor, HuskTowns.getSettings().getServerID(), mayor.getLocation(), name));
        this.greetingMessage = MessageManager.getRawMessage("default_greeting_message", name);
        this.farewellMessage = MessageManager.getRawMessage("default_farewell_message", name);
        this.bio = MessageManager.getRawMessage("default_town_bio");
        this.foundedTimestamp = Instant.now().getEpochSecond();
        this.members.put(mayor.getUniqueId(), TownRole.getMayorRole());
        this.flags = HuskTowns.getSettings().getDefaultClaimFlags();
    }

    /**
     * Create a town object with the specified parameters
     *
     * @param townName         The name of the town
     * @param claimedChunks    Set of claimed/plot/farm chunks
     * @param members          Map of UUIDs of town members & their role
     * @param townSpawn        Town spawn TeleportationPoint
     * @param moneyDeposited   Amount of money deposited into town
     * @param greetingMessage  The town's greeting message
     * @param farewellMessage  The town's farewell message
     * @param bio              The town's bio message
     * @param foundedTimestamp Unix timestamp value of when the town was founded
     * @param flags            The flag set for this town
     */
    public Town(String townName, HashSet<ClaimedChunk> claimedChunks, HashMap<UUID, TownRole> members, TeleportationPoint townSpawn, boolean spawnPublic, double moneyDeposited, String greetingMessage, String farewellMessage, String bio, long foundedTimestamp, HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> flags) {
        this.name = townName;
        this.claimedChunks.addAll(claimedChunks);
        this.members.putAll(members);
        this.townSpawn = townSpawn;
        this.spawnPublic = spawnPublic;
        this.moneyDeposited = moneyDeposited;
        this.foundedTimestamp = foundedTimestamp;
        if (this.name.equalsIgnoreCase(HuskTowns.getSettings().getAdminTownName())) {
            this.greetingMessage = MessageManager.getRawMessage("admin_claim_greeting_message");
            this.farewellMessage = MessageManager.getRawMessage("admin_claim_farewell_message");;
            this.bio = MessageManager.getRawMessage("admin_town_bio");;
            this.flags = HuskTowns.getSettings().getAdminClaimFlags();
        } else {
            this.greetingMessage = greetingMessage;
            this.farewellMessage = farewellMessage;
            this.bio = bio;
            this.flags = flags;
        }
    }

    public HashSet<ClaimedChunk> getClaimedChunks() {
        return claimedChunks;
    }

    // Returns all the claimed chunks a town has *on the server*
    public HashSet<ClaimedChunk> getServerClaimedChunks() {
        HashSet<ClaimedChunk> serverClaimedChunks = new HashSet<>();
        for (ClaimedChunk chunk : claimedChunks) {
            if (chunk.getServer().equalsIgnoreCase(HuskTowns.getSettings().getServerID())) {
                serverClaimedChunks.add(chunk);
            }
        }
        return serverClaimedChunks;
    }

    public int getClaimedChunksNumber() {
        return claimedChunks.size();
    }

    // Returns the town spawn point (null if not set)
    public TeleportationPoint getTownSpawn() {
        return townSpawn;
    }

    public boolean isSpawnPublic() {
        return spawnPublic;
    }

    /**
     * Get a Map<UUID, TownRole> of all a town's members
     *
     * @return Map of all members to their town role
     */
    public HashMap<UUID, TownRole> getMembers() {
        return members;
    }

    public int getMaxMembers() {
        return TownLimitsUtil.getMaxMembers(getLevel(), name);
    }

    public int getMaximumClaimedChunks() {
        return TownLimitsUtil.getMaxClaims(getLevel(), name);
    }

    public int getLevel() {
        return TownLimitsUtil.getLevel(moneyDeposited);
    }

    public String getName() {
        return name;
    }

    public double getMoneyDeposited() {
        return moneyDeposited;
    }

    public String getGreetingMessage() {
        return greetingMessage;
    }

    public String getFarewellMessage() {
        return farewellMessage;
    }

    public String getBio() {
        return bio;
    }

    // Return all of a town's flags
    public HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> getFlags() {
        return flags;
    }

    // Converts a string into an integer value, used in getting town color
    private static long getStringValue(String string) {
        long value = 0;
        for (String c : string.split("")) {
            value++;
            int characterInt = c.charAt(0);
            value = value * (long) characterInt;
        }
        return value;
    }

    // Returns the calculated randomly-seeded-by-name color of a town, in format #xxxxxx
    public static String getTownColorHex(String townName) {
        // Admin claims should always be a light red
        if (townName.equals(HuskTowns.getSettings().getAdminTownName())) {
            return HuskTowns.getSettings().getAdminTownColor();
        }

        // Generates a random color code to color a town, seeded based on the town name
        Random random = new Random(getStringValue(townName));
        int randomHex = random.nextInt(0xffffff + 1);
        return String.format("#%06x", randomHex);
    }

    // Returns the color object from the town's calculated color
    public static Color getTownColor(String townName) {
        return Color.decode(getTownColorHex(townName));
    }

    public String getTownColorHex() {
        return getTownColorHex(this.name);
    }

    public String getFormattedFoundedTime() {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochSecond(foundedTimestamp));
    }

}
