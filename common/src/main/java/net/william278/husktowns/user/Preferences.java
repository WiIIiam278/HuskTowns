package net.william278.husktowns.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.william278.husktowns.claim.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Represents a user's preferences
 *
 * @since 2.0
 */
public class Preferences {

    @Expose
    @SerializedName("town_chat_talking")
    private boolean townChatTalking;

    @Expose
    @SerializedName("town_chat_spying")
    private boolean townChatSpying;

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
    @SerializedName("teleport_target")
    @Nullable
    private Position teleportTarget;

    /**
     * Get the default user {@link Preferences}
     *
     * @return The default {@link Preferences}
     * @since 2.0
     */
    @NotNull
    public static Preferences getDefaults() {
        return new Preferences(false, false, true, false, false);
    }

    /**
     * <b>Internal use only</b> - Construct {@link Preferences} for a user
     *
     * @param townChatTalking   If the user is talking in town chat
     * @param townChatSpying    If the user is spying on town chat
     * @param townNotifications If the user is receiving town notifications
     * @param autoClaimingLand  If the user is auto-claiming land
     * @param ignoringClaims    If the user is ignoring claims
     * @since 2.0
     */
    private Preferences(boolean townChatTalking, boolean townChatSpying, boolean townNotifications,
                        boolean autoClaimingLand, boolean ignoringClaims) {
        this.townChatTalking = townChatTalking;
        this.townChatSpying = townChatSpying;
        this.townNotifications = townNotifications;
        this.autoClaimingLand = autoClaimingLand;
        this.ignoringClaims = ignoringClaims;
    }

    @SuppressWarnings("unused")
    private Preferences() {
    }

    /**
     * Get if the user is talking in town chat
     *
     * @return {@code true} if the user is talking in town chat, {@code false} otherwise
     * @since 2.0
     */
    public boolean isTownChatTalking() {
        return townChatTalking;
    }

    /**
     * Set if the user is talking in town chat
     *
     * @param townChatTalking {@code true} if the user is talking in town chat, {@code false} otherwise
     * @since 2.0
     */
    public void setTownChatTalking(boolean townChatTalking) {
        this.townChatTalking = townChatTalking;
    }

    /**
     * Get if the user is spying on town chat
     *
     * @return {@code true} if the user is spying on town chat, {@code false} otherwise
     * @since 2.0
     */
    public boolean isTownChatSpying() {
        return townChatSpying;
    }

    /**
     * Set if the user is spying on town chat
     *
     * @param townChatSpying {@code true} if the user is spying on town chat, {@code false} otherwise
     * @since 2.0
     */
    public void setTownChatSpying(boolean townChatSpying) {
        this.townChatSpying = townChatSpying;
    }

    /**
     * Get if the user is receiving town notifications and chat messages
     *
     * @return {@code true} if the user is receiving town notifications, {@code false} otherwise
     * @since 2.0
     */
    public boolean isTownNotifications() {
        return townNotifications;
    }

    /**
     * Set if the user is receiving town notifications and chat messages
     *
     * @param townNotifications {@code true} if the user is receiving town notifications, {@code false} otherwise
     * @since 2.0
     */
    public void setTownNotifications(boolean townNotifications) {
        this.townNotifications = townNotifications;
    }

    /**
     * Get if the user is auto-claiming land
     *
     * @return {@code true} if the user is auto-claiming land, {@code false} otherwise
     * @since 2.0
     */
    public boolean isAutoClaimingLand() {
        return autoClaimingLand;
    }

    /**
     * Set if the user is auto-claiming land
     *
     * @param autoClaimingLand {@code true} if the user is auto-claiming land, {@code false} otherwise
     * @since 2.0
     */
    public void setAutoClaimingLand(boolean autoClaimingLand) {
        this.autoClaimingLand = autoClaimingLand;
    }

    /**
     * Get if the user is ignoring claims
     *
     * @return {@code true} if the user is ignoring claims, {@code false} otherwise
     * @since 2.0
     */
    public boolean isIgnoringClaims() {
        return ignoringClaims;
    }

    /**
     * Set if the user is ignoring claims
     *
     * @param ignoringClaims {@code true} if the user is ignoring claims, {@code false} otherwise
     * @since 2.0
     */
    public void setIgnoringClaims(boolean ignoringClaims) {
        this.ignoringClaims = ignoringClaims;
    }

    /**
     * <b>Internal use only</b> - Get the user's current teleport target. This is used for cross-server teleportation.
     *
     * @return The current teleport target, if the user has one.
     * @since 2.0
     */
    public Optional<Position> getTeleportTarget() {
        return Optional.ofNullable(teleportTarget);
    }

    /**
     * <b>Internal use only</b> - Set the user's current teleport target. This is used for cross-server teleportation.
     *
     * @param target The teleport target
     * @since 2.0
     */
    public void setTeleportTarget(@NotNull Position target) {
        this.teleportTarget = target;
    }

    /**
     * <b>Internal use only</b> - Clear the user's current teleport target. This is used for cross-server teleportation.
     *
     * @since 2.0
     */
    public void clearTeleportTarget() {
        this.teleportTarget = null;
    }

}
