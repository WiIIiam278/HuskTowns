package net.william278.husktowns.town;

import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Rules;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RuleSet {

    private Map<Claim.Type, Rules> rules;

    private RuleSet(Map<Claim.Type, Rules> rules) {
        this.rules = rules;
    }

    @SuppressWarnings("unused")
    private RuleSet() {
    }

    @NotNull
    public static RuleSet of(Map<Claim.Type, Rules> rules) {
        return new RuleSet(rules);
    }

    @NotNull
    public Map<Claim.Type, Rules> getRuleMap() {
        return rules;
    }

}
