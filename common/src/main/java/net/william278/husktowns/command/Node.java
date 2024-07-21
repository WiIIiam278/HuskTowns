/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.husktowns.command;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.ConsoleUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

public abstract class Node implements Executable {

    protected static final String PERMISSION_PREFIX = "husktowns.command";

    protected final HuskTowns plugin;
    private final String name;
    private final List<String> aliases;
    private boolean consoleExecutable = false;
    private boolean operatorCommand = false;

    protected Node(@NotNull String name, @NotNull List<String> aliases, @NotNull HuskTowns plugin) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Command name cannot be blank");
        }
        this.name = name;
        this.aliases = aliases;
        this.plugin = plugin;
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

    public void setConsoleExecutable(boolean consoleExecutable) {
        this.consoleExecutable = consoleExecutable;
    }

    public boolean isOperatorCommand() {
        return operatorCommand;
    }

    public void setOperatorCommand(boolean operatorCommand) {
        this.operatorCommand = operatorCommand;
    }

    protected Optional<Integer> parseIntArg(@NotNull String[] args, int index) {
        try {
            if (args.length > index) {
                return Optional.of(Integer.parseInt(args[index]));
            }
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    protected Optional<Integer> parseTimeArgAsDays(@NotNull String[] args, int index) {
        final Map<String, Integer> units = Map.of("d", 1, "w", 7, "m", 30, "y", 365);
        return parseIntArg(args, index)
            .or(() -> parseStringArg(args, index).flatMap(arg -> units.entrySet().stream()
                .filter(entry -> arg.endsWith(entry.getKey())).findFirst()
                .flatMap(entry -> {
                    try {
                        final String number = arg.substring(0, arg.length() - entry.getKey().length());
                        return Optional.of(Integer.parseInt(number) * entry.getValue());
                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
                        return Optional.empty();
                    }
                })));
    }

    protected Optional<Double> parseDoubleArg(@NotNull String[] args, int index) {
        try {
            if (args.length > index) {
                return Optional.of(Double.parseDouble(args[index]));
            }
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    protected Optional<String> parseStringArg(@NotNull String[] args, int index) {
        if (args.length > index) {
            return Optional.of(args[index]);
        }
        return Optional.empty();
    }

    protected Optional<String> parseGreedyString(@NotNull String[] args, int startIndex) {
        if (args.length > startIndex) {
            final StringJoiner sentence = new StringJoiner(" ");
            for (int i = startIndex; i < args.length; i++) {
                sentence.add(args[i]);
            }
            return Optional.of(sentence.toString().trim());
        }
        return Optional.empty();
    }

}
