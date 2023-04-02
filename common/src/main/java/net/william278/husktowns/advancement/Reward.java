package net.william278.husktowns.advancement;

import com.google.gson.annotations.Expose;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class Reward {

    @Expose
    private Type type;
    @Expose
    private int quantity;
    @Expose
    @Nullable
    private String value;

    private Reward(@NotNull Type type, int quantity, @Nullable String value) {
        this.type = type;
        this.quantity = quantity;
        this.value = value;
    }

    public void give(@NotNull OnlineUser user) {
        // todo
    }

    @SuppressWarnings("unused")
    private Reward() {
    }

    @NotNull
    public static Builder of(@NotNull Type type) {
        return new Builder(type);
    }

    public enum Type {
        TOWN_LEVELS,
        TOWN_MONEY,
        PLAYER_MONEY,
        PLAYER_EXPERIENCE,
        PLAYER_LEVELS,
        PLAYER_ITEMS
    }

    private static class Builder {
        private final Type type;
        private int quantity;
        private String value;

        private Builder(@NotNull Type type) {
            this.type = type;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder value(@NotNull String value) {
            this.value = value;
            return this;
        }

        public Reward build() {
            return new Reward(type, quantity, value);
        }
    }

}
