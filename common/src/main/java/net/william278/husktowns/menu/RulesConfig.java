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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RulesConfig {

    private final HuskTowns plugin;
    private final Town town;
    private final CommandUser viewer;
    private final int longestFlagName;

    private RulesConfig(@NotNull HuskTowns plugin, @NotNull Town town, @NotNull CommandUser viewer) {
        this.plugin = plugin;
        this.town = town;
        this.viewer = viewer;
        this.longestFlagName = Arrays.stream(Flag.values())
                .map(Flag::name)
                .mapToInt(String::length)
                .max().orElse(0);
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
        final Map<Flag, Map<Claim.Type, Boolean>> normalized = new HashMap<>();
        for (Map.Entry<Claim.Type, Rules> entry : town.getRules().getRuleMap().entrySet()) {
            for (Map.Entry<Flag, Boolean> flagEntry : entry.getValue().getFlagMap().entrySet()) {
                normalized.put(flagEntry.getKey(), new HashMap<>() {{
                    put(entry.getKey(), flagEntry.getValue());
                }});
            }
        }
        return normalized.entrySet().stream()
                .map(this::getRuleLine)
                .reduce(Component.empty(), Component::append);
    }

    @NotNull
    private Component getRuleLine(@NotNull Map.Entry<Flag, Map<Claim.Type, Boolean>> entry) {
        Component line = plugin.getLocales().getLocale("town_rules_config_flag_name",
                        entry.getKey().name().toLowerCase())
                .map(MineDown::toComponent).orElse(Component.empty());
        for (int i = entry.getKey().name().length(); i < longestFlagName; i++) {
            line = line.append(Component.space());
        }
        line = line.append(Component.space());
        for (Claim.Type type : Claim.Type.values()) {
            line = line.append(getRuleFlag(entry.getKey(), type, entry.getValue().getOrDefault(type, false)));
            for (int i = 0; i < 2; i++) {
                line = line.append(Component.space());
            }
        }
        return line.append(Component.newline());
    }

    @NotNull
    private Component getRuleFlag(@NotNull Flag flag, @NotNull Claim.Type type, boolean value) {
        return Component.text(plugin.getLocales().getRawLocale(value ? "town_rules_config_flag_true"
                        : "town_rules_config_flag_false").orElse(""))
                .hoverEvent(plugin.getLocales().getLocale("town_rules_config_flag_hover")
                        .map(MineDown::toComponent).orElse(Component.empty()))
                .clickEvent(ClickEvent.runCommand("/husktowns:town rules " + flag.name().toLowerCase() + " "
                                                  + type.name().toLowerCase() + " " + !value));
    }

}
