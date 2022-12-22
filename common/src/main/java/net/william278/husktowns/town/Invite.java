package net.william278.husktowns.town;

import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

public class Invite {

    private int townId;
    private User sender;
    private String targetUsername;

    private Invite(int townId, @NotNull User sender, @NotNull String targetUsername) {
        this.townId = townId;
        this.sender = sender;
        this.targetUsername = targetUsername;
    }

    @SuppressWarnings("unused")
    private Invite() {
    }

    public static Invite create(int townId, @NotNull User sender, @NotNull String targetUsername) {
        return new Invite(townId, sender, targetUsername);
    }

    public int getTownId() {
        return townId;
    }

    @NotNull
    public User getSender() {
        return sender;
    }

    @NotNull
    public String getTargetUsername() {
        return targetUsername;
    }
}
