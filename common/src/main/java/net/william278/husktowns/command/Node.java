package net.william278.husktowns.command;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.ConsoleUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.StringJoiner;

public abstract class Node implements Executable {

    protected static final String PERMISSION_PREFIX = "husktowns.command";

    protected final HuskTowns plugin;
    private final String name;
    private final List<String> aliases;
    private boolean consoleExecutable = false;

    protected Node(@NotNull String name, @NotNull List<String> aliases, @NotNull HuskTowns plugin) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Command name cannot be blank");
        }
        this.name = name;
        this.aliases = aliases;
        this.plugin = plugin;
    }

    protected Node(@NotNull String name, @NotNull List<String> aliases, boolean consoleExecutable, @NotNull HuskTowns plugin) {
        this(name, aliases, plugin);
        this.consoleExecutable = consoleExecutable;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public List<String> getAliases() {
        return aliases;
    }

    public final boolean matchesInput(@NotNull String input) {
        return input.equalsIgnoreCase(getName()) || getAliases().contains(input.toLowerCase());
    }

    @NotNull
    public String getPermission() {
        return new StringJoiner(".")
                .add(PERMISSION_PREFIX)
                .add(getName()).toString();
    }

    public boolean canPerform(@NotNull CommandUser user) {
        if (user instanceof ConsoleUser) {
            return isConsoleExecutable();
        }
        return user.hasPermission(getPermission());
    }

    public boolean isConsoleExecutable() {
        return consoleExecutable;
    }

}
