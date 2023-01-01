package net.william278.husktowns.audit;

import com.google.gson.annotations.Expose;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.StringJoiner;

public class Action {

    @Expose
    @Nullable
    private User user;

    @Expose
    @Nullable
    private String details;

    @Expose
    private Type action;

    private Action(@NotNull Type action, @Nullable User user, @Nullable String details) {
        this.action = action;
        this.user = user;
        this.details = details;
    }

    @SuppressWarnings("unused")
    private Action() {
    }

    @NotNull
    public static Action of(@NotNull User user, @NotNull Type action) {
        return new Action(action, user, null);
    }

    @NotNull
    public static Action of(@NotNull Type action) {
        return new Action(action, null, null);
    }

    @NotNull
    public static Action of(@NotNull User user, @NotNull Type action, @NotNull String details) {
        return new Action(action, user, details);
    }

    public Optional<User> getUser() {
        return Optional.ofNullable(user);
    }

    public Optional<String> getDetails() {
        return Optional.ofNullable(details);
    }

    @NotNull
    public Type getAction() {
        return action;
    }


    @Override
    public String toString() {
        final StringJoiner joiner = new StringJoiner(" ");
        getUser().ifPresent(user -> joiner.add("[" + user.getUsername() + "]"));
        joiner.add(action.toString());
        getDetails().ifPresent(joiner::add);
        return joiner.toString();
    }

    public enum Type {
        CREATE_TOWN,
        CREATE_CLAIM,
        DELETE_CLAIM, RENAME_TOWN, UPDATE_BIO, UPDATE_GREETING, UPDATE_FAREWELL, UPDATE_COLOR, UPDATE_SPAWN, UPDATE_SPAWN_IS_PUBLIC, CLEAR_SPAWN, MAKE_CLAIM_PLOT, MAKE_CLAIM_FARM, MAKE_CLAIM_REGULAR, ADD_PLOT_MEMBER, REMOVE_PLOT_MEMBER, DEPOSIT_MONEY, WITHDRAW_MONEY, SET_FLAG_RULE, LEVEL_UP, EVICT, MEMBER_JOIN, TRANSFER_OWNERSHIP,
    }

}
