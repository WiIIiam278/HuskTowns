package me.william278.bungeetowny.objects.town;

import me.william278.bungeetowny.HuskTowns;
import me.william278.bungeetowny.objects.chunk.ClaimedChunk;
import me.william278.bungeetowny.objects.teleport.TeleportationPoint;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;

public class Town {

    // Set of all chunks claimed by the town, including PlotChunks and FarmChunks
    private HashSet<ClaimedChunk> claimedChunks;

    // TeleportationPoint of the town's spawn position
    private TeleportationPoint townSpawn;

    // HashSet of all town members
    private HashSet<UUID> memberUUIDs;

    // Resident, Trusted & Mayor holder UUIDs
    private UUID mayorUUID;
    private HashSet<UUID> residentUUIDs;
    private HashSet<UUID> trustedUUIDs;

    // Current town Level (dictates max members, etc)
    private int townLevel;

    // Amount of money deposited into town
    private double moneyDeposited;

    public Town() {

    }

    /**
     * Create a new Town at the Mayor's position
     * @param mayor the Player who will be the mayor of the town
     */
    public Town(Player mayor) {
        this.mayorUUID = mayor.getUniqueId();
        this.townSpawn = new TeleportationPoint(mayor.getLocation(), HuskTowns.getSettings().getServerID());
        this.townLevel = 1;
        this.moneyDeposited = 0D;

        this.memberUUIDs = new HashSet<>();
        this.residentUUIDs = new HashSet<>();
        this.trustedUUIDs = new HashSet<>();

        this.claimedChunks = new HashSet<>();
        this.claimedChunks.add(new ClaimedChunk(mayor));

        this.memberUUIDs.add(mayorUUID);
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
     * Get the UUID of a town's Mayor
     * @return UUID of the town's Mayor
     */
    public UUID getMayor() {
        return mayorUUID;
    }

    /**
     * Get a Set<UUID> of members of a town with the Trusted rank
     * @return HashSet<UUID> of town Trusted players
     */
    public HashSet<UUID> getTrusted() {
        return trustedUUIDs;
    }

    /**
     * Get a Set<UUID> of members of a town with the Resident rank
     * @return HashSet<UUID> of town Resident players
     */
    public HashSet<UUID> getResidents() {
        return residentUUIDs;
    }

    /**
     * Get a Set<UUID> of all a town's members
     * @return HashSet<UUID> of town members
     */
    public HashSet<UUID> getMembers() {
        return memberUUIDs;
    }

    public int getTownLevel() {
        return townLevel;
    }

    public double getMoneyDeposited() {
        return moneyDeposited;
    }
}
