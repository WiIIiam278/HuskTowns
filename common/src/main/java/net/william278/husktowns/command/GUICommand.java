package net.william278.husktowns.command;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GUICommand extends ChildCommand implements TownTabProvider {
    private final GUIType type;

    protected GUICommand(@NotNull Command parent, @NotNull HuskTowns plugin, @NotNull GUIType type) {
        super(type.name, type.aliases, parent, "[town]", plugin);
        this.type = type;
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        final Optional<String> townName = parseStringArg(args, 0);

        Optional<Town> optionalTown;
        if (townName.isEmpty()) {
            if (executor instanceof OnlineUser user) {
                optionalTown = plugin.getUserTown(user).map(Member::town);
                if (optionalTown.isEmpty()) {
                    plugin.getLocales().getLocale("error_not_in_town")
                            .ifPresent(executor::sendMessage);
                    return;
                }
            } else {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                        .ifPresent(executor::sendMessage);
                return;
            }
        } else {
            optionalTown = plugin.findTown(townName.get());
        }

        if (optionalTown.isEmpty()) {
            plugin.getLocales().getLocale("error_town_not_found", townName.orElse(""))
                    .ifPresent(executor::sendMessage);
            return;
        }

        final Town town = optionalTown.get();
        switch (type) {
            case TOWN_OVERVIEW -> plugin.getGUIManager().openTownGUI(executor, town);
            case DEEDS -> plugin.getGUIManager().openDeedsGUI(executor, town);
            case CENSUS -> plugin.getGUIManager().openCensusGUI(executor, town);
            case TOWN_LIST -> plugin.getGUIManager().openTownListGUI(executor, town);
        }
    }

    @NotNull
    @Override
    public ConcurrentLinkedQueue<Town> getTowns() {
        return plugin.getTowns();
    }


    public enum GUIType {
        TOWN_OVERVIEW("info", "about"),
        TOWN_LIST("list", "l"),
        DEEDS("deeds", "claims", "claimlist"),
        CENSUS("census", "members", "memberlist");

        private final String name;
        private final List<String> aliases;

        GUIType(@NotNull String name, @NotNull String... aliases) {
            this.name = name;
            this.aliases = List.of(aliases);
        }
    }
}
