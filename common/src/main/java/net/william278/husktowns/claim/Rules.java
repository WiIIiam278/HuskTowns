package net.william278.husktowns.claim;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Claim rules, defining what players can do in a claim
 */
public class Rules {

    private Map<Flag, Boolean> rules;

    private Rules(@NotNull Map<Flag, Boolean> rules) {
        this.rules = rules;
    }

    @NotNull
    public static Rules of(@NotNull Map<Flag, Boolean> rules) {
        return new Rules(rules);
    }

    @SuppressWarnings("unused")
    private Rules() {
    }

    public void setFlag(@NotNull Flag flag, boolean value) {
        if (rules.containsKey(flag)) {
            rules.replace(flag, value);
        } else {
            rules.put(flag, value);
        }
    }

    public boolean isFlagSet(@NotNull Flag flag) {
        return rules.getOrDefault(flag, false);
    }

}
