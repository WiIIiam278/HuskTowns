package net.william278.husktowns.menu;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Flag;
import net.william278.husktowns.claim.Rules;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class RulesConfig {

    private final HuskTowns plugin;
    private final Town town;
    private final CommandUser viewer;

    private RulesConfig(@NotNull HuskTowns plugin, @NotNull Town town, @NotNull CommandUser viewer) {
        this.plugin = plugin;
        this.town = town;
        this.viewer = viewer;
    }

    @NotNull
    public static RulesConfig of(@NotNull HuskTowns plugin, @NotNull Town town, @NotNull CommandUser viewer) {
        return new RulesConfig(plugin, town, viewer);
    }

    @NotNull
    public Component toComponent() {
        return getTitle()
                .append(getRules());
    }

    public void show() {
        viewer.sendMessage(toComponent());
    }

    @NotNull
    private Component getTitle() {
        return plugin.getLocales().getLocale("town_rules_config_title", town.getName())
                .map(mineDown -> mineDown.toComponent().append(Component.newline()))
                .orElse(Component.empty());
    }

    @NotNull
    private Component getRules() {
        final Map<Flag, Map<Claim.Type, Boolean>> rules = new TreeMap<>();
        for (Map.Entry<Claim.Type, Rules> entry : town.getRules().entrySet()) {
            for (Map.Entry<Flag, Boolean> flagEntry : entry.getValue().getFlagMap().entrySet()) {
                if (rules.containsKey(flagEntry.getKey())) {
                    rules.get(flagEntry.getKey()).put(entry.getKey(), flagEntry.getValue());
                } else {
                    Map<Claim.Type, Boolean> claimTypeMap = new HashMap<>();
                    claimTypeMap.put(entry.getKey(), flagEntry.getValue());
                    rules.put(flagEntry.getKey(), claimTypeMap);
                }
            }
        }
        return rules.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().name().toLowerCase()))
                .map(this::getRuleLine)
                .reduce(Component.empty(), Component::append);
    }

    @NotNull
    private Component getRuleLine(@NotNull Map.Entry<Flag, Map<Claim.Type, Boolean>> entry) {
        Component line = Component.newline();
        for (Claim.Type type : entry.getValue().keySet()) {
            line = line.append(getRuleFlag(entry.getKey(), type, entry.getValue().get(type)));
            for (int i = 0; i < 3; i++) {
                line = line.append(Component.space());
            }
        }
        return line.append(plugin.getLocales().getLocale("town_rules_config_flag_name",
                        entry.getKey().name().toLowerCase())
                .map(MineDown::toComponent).orElse(Component.empty()));
    }

    @NotNull
    private Component getRuleFlag(@NotNull Flag flag, @NotNull Claim.Type type, boolean value) {
        return plugin.getLocales().getLocale(value ? "town_rules_config_flag_true" : "town_rules_config_flag_false")
                .map(MineDown::toComponent).orElse(Component.empty())
                .hoverEvent(plugin.getLocales().getLocale("town_rules_config_flag_hover",
                        type.name().toLowerCase()).map(MineDown::toComponent).orElse(Component.empty()))
                .clickEvent(ClickEvent.runCommand("/husktowns:town rules " + flag.name().toLowerCase() + " "
                                                  + type.name().toLowerCase() + " " + !value + " -m"));
    }

}
