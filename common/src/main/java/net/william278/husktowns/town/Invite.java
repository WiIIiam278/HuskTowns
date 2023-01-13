package net.william278.husktowns.town;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an invitation sent to a player asking them to join a town
 */
public class Invite {

    @Expose
    @SerializedName("town_id")
    private int townId;

    @Expose
    private User sender;

    private Invite(int townId, @NotNull User sender) {
        this.townId = townId;
        this.sender = sender;
    }

    @SuppressWarnings("unused")
    private Invite() {
    }

    @NotNull
    public static Invite create(int townId, @NotNull User sender) {
        return new Invite(townId, sender);
    }

    public int getTownId() {
        return townId;
    }

    @NotNull
    public User getSender() {
        return sender;
    }

}
