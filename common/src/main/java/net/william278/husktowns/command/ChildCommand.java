package net.william278.husktowns.command;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.CommandUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.StringJoiner;

public abstract class ChildCommand extends Node implements TabProvider {
    private final String parentName;

    protected ChildCommand(@NotNull String name, @NotNull List<String> aliases, boolean consoleExecutable,
                           @NotNull Command parent, @NotNull HuskTowns plugin) {
        super(name, aliases, consoleExecutable, plugin);
        this.parentName = parent.getName();
    }

    protected ChildCommand(@NotNull String name, @NotNull List<String> aliases, @NotNull Command parent,
                           @NotNull HuskTowns plugin) {
        super(name, aliases, plugin);
        this.parentName = parent.getName();
    }

    @Override
    @NotNull
    public String getPermission() {
        return new StringJoiner(".")
                .add(super.getPermission().substring(0, super.getPermission().lastIndexOf(".")))
                .add(parentName)
                .add(getName()).toString();
    }

}
