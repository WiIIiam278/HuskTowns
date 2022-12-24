package net.william278.husktowns.audit;

import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Action {

    @Nullable
    private User user;

    private Type action;

    private Action(@Nullable User user, @NotNull Type action) {
        this.user = user;
        this.action = action;
    }

    @SuppressWarnings("unused")
    private Action() {
    }

    @NotNull
    public static Action user(@NotNull User user, @NotNull Type action) {
        return new Action(user, action);
    }

    @NotNull
    public static Action of(@NotNull Type action) {
        return new Action(null, action);
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
        return (user != null ? "[" + user.getUsername() + "] " : "") + action.name().toLowerCase();
    }

    public enum Type {
        CREATE_TOWN
    }

}
