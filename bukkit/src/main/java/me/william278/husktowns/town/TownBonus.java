package me.william278.husktowns.town;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.UUID;

public record TownBonus(UUID applierUUID, int bonusClaims, int bonusMembers, long appliedTimestamp) {

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
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochSecond(appliedTimestamp));
    }

}
