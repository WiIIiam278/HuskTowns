package me.william278.husktowns.object.town;

import me.william278.husktowns.HuskTowns;

import java.time.Instant;
import java.util.UUID;

public class TownInvite {

    private final UUID inviter;
    private final String townName;
    private final long expiry;

    public TownInvite(UUID inviter, String townName) {
        this.inviter = inviter;
        this.townName = townName;
        this.expiry = Instant.now().getEpochSecond() + HuskTowns.getSettings().getInviteExpiryTime();
    }

    public TownInvite(String townName, UUID inviter, long expiry) {
        this.inviter = inviter;
        this.townName = townName;
        this.expiry = expiry;
    }

    public UUID getInviter() {
        return inviter;
    }

    public String getTownName() {
        return townName;
    }

    public long getExpiry() {
        return expiry;
    }

    public boolean hasExpired() {
        return Instant.now().getEpochSecond() > getExpiry();
    }
}
