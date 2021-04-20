package me.william278.bungeetowny.object.town;

import me.william278.bungeetowny.HuskTowns;
import me.william278.bungeetowny.object.chunk.ClaimedChunk;
import me.william278.bungeetowny.object.teleport.TeleportationPoint;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class Town {

    // Set of all chunks claimed by the town, including PlotChunks and FarmChunks
    private HashSet<ClaimedChunk> claimedChunks;

    // TeleportationPoint of the town's spawn position
    private TeleportationPoint townSpawn;

    // HashSet of all town members
    private HashMap<UUID, TownRole> memberUUIDs;

    // Name of the town
    private String name;

    // Current town Level (dictates max members, etc)
    private int level;

    // Amount of money deposited into town
    private double moneyDeposited;

    public Town() {

    }

    /**
     * Create a new Town at the Mayor's position
     * @param mayor the Player who will be the mayor of the town
     */
    public Town(Player mayor, String name) {
        this.townSpawn = new TeleportationPoint(mayor.getLocation(), HuskTowns.getSettings().getServerID());
        this.level = 1;
        this.moneyDeposited = 0D;
        this.name = name;

        this.memberUUIDs = new HashMap<>();

        this.claimedChunks = new HashSet<>();
        this.claimedChunks.add(new ClaimedChunk(mayor));

        this.memberUUIDs.put(mayor.getUniqueId(), TownRole.MAYOR);
    }

    private HashSet<ClaimedChunk> getClaimedChunks() {
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

    // Returns the town spawn point (null if not set)
    public TeleportationPoint getTownSpawn() {
        return townSpawn;
    }

    /**
     * Get a Map<UUID, TownRole> of all a town's members
     * @return Map of all members to their town role
     */
    public HashMap<UUID, TownRole> getMembers() {
        return memberUUIDs;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public double getMoneyDeposited() {
        return moneyDeposited;
    }
}
