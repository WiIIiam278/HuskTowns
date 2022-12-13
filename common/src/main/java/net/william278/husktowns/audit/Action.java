package net.william278.husktowns.audit;

import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.StringJoiner;

public class Action {

    private LocalDateTime timestamp;

    @Nullable
    private User user;

    private Type action;

    private Action(@Nullable User user, Type action) {
        this.user = user;
        this.action = action;
        this.timestamp = LocalDateTime.now();
    }

    @SuppressWarnings("unused")
    private Action() {
    }

    @NotNull
    public static Action now(@NotNull User user, @NotNull Type action) {
        return new Action(user, action);
    }

    public static Action now(@NotNull Type action) {
        return new Action(null, action);
    }

    @NotNull
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Nullable
    public User getUser() {
        return user;
    }

    @NotNull
    public Type getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "[" + timestamp.toString() + "] "
                + (user != null ? user.getUsername() + ": " : "")
                + action.name().toLowerCase();
    }

    public enum Type {

    }

}
