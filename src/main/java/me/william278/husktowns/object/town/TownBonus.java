package me.william278.husktowns.object.town;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.UUID;

public class TownBonus {

    private final int bonusClaims;
    private final int bonusMembers;
    private final UUID applierUUID;
    private final long appliedTimestamp;

    public TownBonus(UUID applierUUID, int bonusClaims, int bonusMembers, long appliedTimestamp) {
        this.bonusClaims = bonusClaims;
        this.bonusMembers = bonusMembers;
        this.applierUUID = applierUUID;
        this.appliedTimestamp = appliedTimestamp;
    }

    public int getBonusClaims() {
        return bonusClaims;
    }

    public int getBonusMembers() {
        return bonusMembers;
    }

    public UUID getApplierUUID() {
        return applierUUID;
    }

    public long getAppliedTimestamp() {
        return appliedTimestamp;
    }

    public String getFormattedAppliedTime() {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochSecond(appliedTimestamp));
    }

}
