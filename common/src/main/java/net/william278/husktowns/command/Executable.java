package net.william278.husktowns.command;

import net.william278.husktowns.user.CommandUser;
import org.jetbrains.annotations.NotNull;

public interface Executable {

    void execute(@NotNull CommandUser executor, @NotNull String[] args);

}
