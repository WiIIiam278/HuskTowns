package net.william278.husktowns.command;

import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface TownTabProvider extends TabProvider {

    @Override
    @NotNull
    default List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
        return args.length == 1 ? filter(getTownNames(), args) : List.of();
    }

    @NotNull
    default List<String> getTownNames() {
        return getTowns().stream().map(Town::getName).sorted().toList();
    }

    @NotNull
    ConcurrentLinkedQueue<Town> getTowns();

}
