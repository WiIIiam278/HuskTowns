package net.william278.husktowns.claim;

import com.google.gson.annotations.Expose;
import net.william278.husktowns.listener.Operation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Claim rules, defining what players can do in a claim
 */
public class Rules {

    @Expose
    private Map<Flag, Boolean> flags;

    private Rules(@NotNull Map<Flag, Boolean> flags) {
        this.flags = flags;
    }

    @NotNull
    public static Rules of(@NotNull Map<Flag, Boolean> rules) {
        return new Rules(rules);
    }

    @SuppressWarnings("unused")
    private Rules() {
    }

    public void setFlag(@NotNull Flag flag, boolean value) {
        if (flags.containsKey(flag)) {
            flags.replace(flag, value);
        } else {
            flags.put(flag, value);
        }
    }

    public boolean isFlagSet(@NotNull Flag flag) {
        return flags.getOrDefault(flag, false);
    }

    @NotNull
    public Map<Flag, Boolean> getFlagMap() {
        return flags;
    }

    public boolean isOperationAllowed(@NotNull Operation.Type type) {
        return flags.entrySet().stream().anyMatch(entry -> entry.getKey().isOperationAllowed(type) && entry.getValue());
    }
}
