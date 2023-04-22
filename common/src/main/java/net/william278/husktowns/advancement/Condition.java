/*
 * This file is part of HuskTowns by William278. Do not redistribute!
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  All rights reserved.
 *
 *  This source code is provided as reference to licensed individuals that have purchased the HuskTowns
 *  plugin once from any of the official sources it is provided. The availability of this code does
 *  not grant you the rights to modify, re-distribute, compile or redistribute this source code or
 *  "plugin" outside this intended purpose. This license does not cover libraries developed by third
 *  parties that are utilised in the plugin.
 */

package net.william278.husktowns.advancement;

import com.google.gson.annotations.Expose;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.Function;

public class Condition<T> {

    @Expose
    private String variable;
    @Expose
    private Operation operation;
    @Expose
    private String value;

    private Condition(@NotNull Variable<T> variable, @NotNull Operation operation, @NotNull T value) {
        this.variable = variable.getIdentifier();
        this.operation = operation;
        this.value = Variable.valueToString(value);
    }

    @SuppressWarnings("unused")
    private Condition() {
    }

    public boolean isMet(@NotNull Town town, @NotNull OnlineUser user) {
        return this.operation.isSatisfied(this.resolveIdentifier().resolve(town, user, value));
    }

    @NotNull
    public String getKey() {
        return this.variable + "_" + operation.name().toLowerCase(Locale.ENGLISH) + "_" + value;
    }

    @SuppressWarnings("unchecked")
    private Variable<T> resolveIdentifier() throws IllegalArgumentException {
        return (Variable<T>) Variable.parseVariable(variable)
                .orElseThrow(() -> new IllegalArgumentException("Unable to resolve variable type"));
    }

    @NotNull
    public static <T> Condition.Builder<T> of(@NotNull Variable<T> variable) {
        return new Condition.Builder<>(variable);
    }

    private enum Operation {
        IS((value) -> value == 0),
        GREATER_THAN((value) -> value > 0),
        LESS_THAN((value) -> value < 0);

        private final Function<Integer, Boolean> isSatisfied;

        Operation(@NotNull Function<Integer, Boolean> isSatisfied) {
            this.isSatisfied = isSatisfied;
        }

        public boolean isSatisfied(@NotNull Integer value) {
            return isSatisfied.apply(value);
        }
    }

    public static class Builder<T> {

        private final Variable<T> variable;
        private Operation operation;
        private T value;

        protected Builder(@NotNull Variable<T> variable) {
            this.variable = variable;
        }

        @NotNull
        private Builder<T> operation(@NotNull Operation operation) {
            this.operation = operation;
            return this;
        }

        @NotNull
        public Builder<T> is() {
            return operation(Operation.IS);
        }

        @NotNull
        public Builder<T> greaterThan() {
            return operation(Operation.GREATER_THAN);
        }

        @NotNull
        public Builder<T> lessThan() {
            return operation(Operation.LESS_THAN);
        }

        @NotNull
        public Builder<T> value(@NotNull T value) {
            this.value = value;
            return this;
        }

        @NotNull
        public Condition<T> build() {
            return new Condition<>(variable, operation, value);
        }

    }

}
