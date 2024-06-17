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

package net.william278.husktowns.advancement;

import com.google.gson.annotations.Expose;
import net.kyori.adventure.key.Key;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Action;
import net.william278.husktowns.user.OnlineUser;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Objects;

@SuppressWarnings("unused")
public class Reward {

    @Expose
    private Type type;
    @Expose
    private int quantity;
    @Expose
    @Nullable
    private String value;

    private Reward(@NotNull Type type, int quantity, @Nullable String value) {
        this.type = type;
        this.quantity = quantity;
        this.value = value;
    }

    public void give(@NotNull OnlineUser user, @NotNull HuskTowns plugin) {
        switch (type) {
            case TOWN_LEVELS -> plugin.getUserTown(user).ifPresent(member -> plugin.getManager()
                .editTown(user, member.town(), town -> {
                    town.getLog().log(Action.of(user, Action.Type.ADVANCEMENT_REWARD_LEVELS, "Leveled up: " + quantity));
                    town.setLevel(Math.min(plugin.getLevels().getMaxLevel(), town.getLevel() + quantity));
                }));
            case TOWN_MONEY -> plugin.getUserTown(user).ifPresent(member -> plugin.getManager()
                .editTown(user, member.town(), town -> {
                    final BigDecimal quantity = BigDecimal.valueOf(this.quantity);
                    town.getLog().log(Action.of(user, Action.Type.ADVANCEMENT_REWARD_MONEY, "Awarded: " + quantity));
                    town.setMoney(town.getMoney().add(quantity));
                }));
            case TOWN_BONUS_CLAIMS -> plugin.getUserTown(user).ifPresent(member -> plugin.getManager()
                .editTown(user, member.town(), town -> {
                    town.getLog().log(Action.of(user, Action.Type.ADVANCEMENT_REWARD_BONUS_CLAIMS, "Bonus claims: " + quantity));
                    town.setBonusClaims(town.getBonusClaims() + quantity);
                }));
            case TOWN_BONUS_MEMBERS -> plugin.getUserTown(user).ifPresent(member -> plugin.getManager()
                .editTown(user, member.town(), town -> {
                    town.getLog().log(Action.of(user, Action.Type.ADVANCEMENT_REWARD_BONUS_MEMBERS, "Bonus members: " + quantity));
                    town.setBonusMembers(town.getBonusMembers() + quantity);
                }));
            case PLAYER_MONEY -> plugin.getEconomyHook().ifPresent(hook -> hook
                .giveMoney(user, BigDecimal.valueOf(quantity), "Advancement Reward"));
            case PLAYER_EXPERIENCE -> user.giveExperiencePoints(quantity);
            case PLAYER_LEVELS -> user.giveExperienceLevels(quantity);
            case PLAYER_ITEMS -> {
                final @Subst("minecraft:stone") String material = Objects.requireNonNull(value);
                user.giveItem(Key.key(material), quantity);
            }
            case RUN_COMMAND -> {
                if (value == null) {
                    throw new IllegalStateException("Cannot run command with null value");
                }
                for (int i = 0; i < quantity; i++) {
                    plugin.dispatchCommand(value.replaceAll("%player%", user.getUsername())
                        .replaceAll("%uuid%", user.getUuid().toString()));
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private Reward() {
    }

    @NotNull
    public static Builder of(@NotNull Type type) {
        return new Builder(type);
    }

    public enum Type {
        TOWN_LEVELS,
        TOWN_MONEY,
        PLAYER_MONEY,
        PLAYER_EXPERIENCE,
        PLAYER_LEVELS,
        PLAYER_ITEMS,
        TOWN_BONUS_CLAIMS,
        TOWN_BONUS_MEMBERS,
        RUN_COMMAND
    }

    public static class Builder {
        private final Type type;
        private int quantity = 1;
        private String value;

        private Builder(@NotNull Type type) {
            this.type = type;
        }

        @NotNull
        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        @NotNull
        public Builder value(@NotNull String value) {
            this.value = value;
            return this;
        }

        @NotNull
        public Reward build() {
            return new Reward(type, quantity, value);
        }
    }

}
