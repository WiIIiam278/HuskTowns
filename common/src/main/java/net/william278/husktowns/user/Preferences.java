package net.william278.husktowns.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.william278.husktowns.claim.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Preferences {

    @Expose
    @SerializedName("town_chat_talking")
    private boolean townChatTalking;

    @Expose
    @SerializedName("town_notifications")
    private boolean townNotifications;

    @Expose
    @SerializedName("auto_claiming_land")
    private boolean autoClaimingLand;

    @Expose
    @SerializedName("ignoring_claims")
    private boolean ignoringClaims;

    @Expose
    @Nullable
    private Position currentTeleportTarget;

    @NotNull
    public static Preferences getDefaults() {
        return new Preferences(false, true, false, false);
    }

    private Preferences(boolean townChatTalking, boolean townNotifications, boolean autoClaimingLand, boolean ignoringClaims) {
        this.townChatTalking = townChatTalking;
        this.townNotifications = townNotifications;
        this.autoClaimingLand = autoClaimingLand;
        this.ignoringClaims = ignoringClaims;
    }

    @SuppressWarnings("unused")
    private Preferences() {
    }

    public boolean isTownChatTalking() {
        return townChatTalking;
    }

    public void setTownChatTalking(boolean townChatTalking) {
        this.townChatTalking = townChatTalking;
    }

    public boolean isTownNotifications() {
        return townNotifications;
    }

    public void setTownNotifications(boolean townNotifications) {
        this.townNotifications = townNotifications;
    }

    public boolean isAutoClaimingLand() {
        return autoClaimingLand;
    }

    public void setAutoClaimingLand(boolean autoClaimingLand) {
        this.autoClaimingLand = autoClaimingLand;
    }

    public boolean isIgnoringClaims() {
        return ignoringClaims;
    }

    public void setIgnoringClaims(boolean ignoringClaims) {
        this.ignoringClaims = ignoringClaims;
    }

    public Optional<Position> getCurrentTeleportTarget() {
        return Optional.ofNullable(currentTeleportTarget);
    }

    public void setCurrentTeleportTarget(@NotNull Position currentTeleportTarget) {
        this.currentTeleportTarget = currentTeleportTarget;
    }

    public void clearCurrentTeleportTarget() {
        this.currentTeleportTarget = null;
    }

}
