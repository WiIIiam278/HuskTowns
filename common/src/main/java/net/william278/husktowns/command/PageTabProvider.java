package net.william278.husktowns.command;

import net.william278.husktowns.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface PageTabProvider extends TabProvider {

    @Override
    @NotNull
    default List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
        return args.length == 1 ? filter(getPageNumbers(), args) : List.of();
    }

    default List<String> getPageNumbers() {
        final List<String> numbers = new ArrayList<>();
        for (int i = 1; i <= getPageCount(); i++) {
            numbers.add(Integer.toString(i));
        }
        return numbers;
    }

    int getPageCount();

}
