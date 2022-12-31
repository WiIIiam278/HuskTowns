package net.william278.husktowns.command;

import net.william278.husktowns.HuskTowns;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public abstract class ChildCommand extends Node {
    protected final Command parent;
    private final String usage;

    protected ChildCommand(@NotNull String name, @NotNull List<String> aliases, @NotNull Command parent,
                           @NotNull String usage, @NotNull HuskTowns plugin) {
        super(name, aliases, plugin);
        this.parent = parent;
        this.usage = usage;
    }

    @Override
    @NotNull
    public String getPermission() {
        return new StringJoiner(".")
                .add(super.getPermission().substring(0, super.getPermission().lastIndexOf(".")))
                .add(parent.getName())
                .add(getName()).toString();
    }

    @NotNull
    public String getUsage() {
        return "/" + parent.getName() + " " + getName() + ((" " + usage).isBlank() ? "" : " " + usage);
    }

    @NotNull
    public final Optional<String> getDescription() {
        return plugin.getLocales().getRawLocale("command_" + parent.getName() + "_" + getName() + "_description");
    }

}
