package net.william278.husktowns.town;

import net.william278.husktowns.HuskTowns;

import java.time.Instant;

public class TownInvite {

    private final String inviter;
    private final String townName;
    private final long expiry;

    public TownInvite(String inviter, String townName) {
        this.inviter = inviter;
        this.townName = townName;
        this.expiry = Instant.now().getEpochSecond() + HuskTowns.getSettings().getInviteExpiryTime();
    }

    public TownInvite(String townName, String inviter, long expiry) {
        this.inviter = inviter;
        this.townName = townName;
        this.expiry = expiry;
    }

    public String getInviter() {
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
