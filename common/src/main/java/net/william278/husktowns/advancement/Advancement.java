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
import com.google.gson.annotations.SerializedName;
import net.kyori.adventure.key.Key;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Advancement {

    @NotNull
    public static final Advancement DEFAULT_ADVANCEMENTS = Advancement.builder()
        .title("Towns")
        .description("Build and grow a thriving town community")
        .icon(Key.key("bell"))
        .sendNotification(false)
        .child(takingRoot -> takingRoot
            .title("Taking Root")
            .description("Create or join a town")
            .icon(Key.key("oak_sign"))
            .condition(Condition.that(Variable.USER_IN_TOWN).is().value(true))
            .reward(Reward.of(Reward.Type.PLAYER_EXPERIENCE).quantity(50))
            .child(members2 -> members2
                .title("You Are (Not) Alone")
                .description("Have another member join your town")
                .icon(Key.key("zombie_head"))
                .condition(Condition.that(Variable.TOWN_MEMBERS).greaterThan().value(1))
                .child(fallenKingdom -> fallenKingdom
                    .title("Fallen Kingdom")
                    .description("Have at least 15 unique players leave your town")
                    .icon(Key.key("wither_skeleton_skull"))
                    .condition(Condition.that(Variable.MEMBERS_LEFT).greaterThan().value(14))
                )
                .child(members8 -> members8
                    .title("Eight's a Crowd")
                    .description("Have 8 members in your town")
                    .icon(Key.key("player_head"))
                    .condition(Condition.that(Variable.TOWN_MEMBERS).greaterThan().value(7))
                    .child(members16 -> members16
                        .title("Bustling Community")
                        .description("Have 16 members in your town")
                        .icon(Key.key("creeper_head"))
                        .condition(Condition.that(Variable.TOWN_MEMBERS).greaterThan().value(15))
                        .reward(Reward.of(Reward.Type.PLAYER_EXPERIENCE).quantity(200))
                        .child(members32 -> members32
                            .title("City Community")
                            .description("Have 32 members in your town")
                            .icon(Key.key("dragon_head"))
                            .frame(Frame.CHALLENGE)
                            .condition(Condition.that(Variable.TOWN_MEMBERS).greaterThan().value(31))
                        )
                    )
                )
                .child(townTransferred -> townTransferred
                    .title("Changing Of The Guard")
                    .description("Have the ownership of your town transferred to another player")
                    .icon(Key.key("iron_sword"))
                    .condition(Condition.that(Variable.TOWN_MAYOR_CHANGED).greaterThan().value(0))
                )
            )
            .child(weekOldTown -> weekOldTown
                .title("Established")
                .description("Have your town be at least a week old")
                .icon(Key.key("clock"))
                .condition(Condition.that(Variable.TOWN_FOUNDED).greaterThan().value(OffsetDateTime.now().minusWeeks(1)))
            )
            .child(claim1 -> claim1
                .title("New Horizons")
                .description("Claim the first chunk of land for your town")
                .icon(Key.key("dirt"))
                .condition(Condition.that(Variable.TOWN_CLAIMS).greaterThan().value(0))
                .reward(Reward.of(Reward.Type.PLAYER_EXPERIENCE).quantity(50))
                .child(claim5 -> claim5
                    .title("Village Expansion")
                    .description("Claim 6 chunks of land for your town")
                    .icon(Key.key("oak_log"))
                    .condition(Condition.that(Variable.TOWN_CLAIMS).greaterThan().value(5))
                    .reward(Reward.of(Reward.Type.PLAYER_ITEMS).quantity(16).value("minecraft:oak_log"))
                    .child(claim15 -> claim15
                        .title("Officially A Town")
                        .description("Claim 18 chunks of land for your town")
                        .icon(Key.key("stone_bricks"))
                        .frame(Frame.GOAL)
                        .condition(Condition.that(Variable.TOWN_CLAIMS).greaterThan().value(17))
                        .child(claim30 -> claim30
                            .title("City Folk")
                            .description("Claim 40 chunks of land for your town")
                            .icon(Key.key("gray_concrete"))
                            .condition(Condition.that(Variable.TOWN_CLAIMS).greaterThan().value(39))
                            .child(claim60 -> claim60
                                .title("The Empire Business")
                                .description("Claim 80 chunks of land for your town")
                                .icon(Key.key("beacon"))
                                .frame(Frame.CHALLENGE)
                                .condition(Condition.that(Variable.TOWN_CLAIMS).greaterThan().value(79))
                                .reward(Reward.of(Reward.Type.PLAYER_EXPERIENCE).quantity(500))
                            )
                        )
                    )
                )
                .child(soreMachi -> soreMachi
                    .title("And Yet The Town Moves")
                    .description("Move the town spawn position somewhere else")
                    .icon(Key.key("ender_eye"))
                    .condition(Condition.that(Variable.TOWN_HAS_SPAWN).is().value(true))
                    .condition(Condition.that(Variable.TOWN_CHANGED_SPAWN).greaterThan().value(0))
                )
                .child(greetingFarewell -> greetingFarewell
                    .title("Well Signposted")
                    .description("Set both a greeting and a farewell message for your town")
                    .icon(Key.key("paper"))
                    .condition(Condition.that(Variable.TOWN_HAS_GREETING).is().value(true))
                    .condition(Condition.that(Variable.TOWN_HAS_FAREWELL).is().value(true))
                    .reward(Reward.of(Reward.Type.PLAYER_EXPERIENCE).quantity(50))
                )
            )
            .child(level2 -> level2
                .title("Level Up")
                .description("Level up your town to Level 2")
                .icon(Key.key("experience_bottle"))
                .condition(Condition.that(Variable.TOWN_LEVEL).greaterThan().value(1))
                .child(level5 -> level5
                    .title("Five Guys")
                    .description("Level up your town to Level 5")
                    .icon(Key.key("nether_brick"))
                    .condition(Condition.that(Variable.TOWN_LEVEL).greaterThan().value(4))
                    .reward(Reward.of(Reward.Type.PLAYER_EXPERIENCE).quantity(50))
                    .frame(Frame.GOAL)
                    .child(level10 -> level10
                        .title("High Roller")
                        .description("Level up your town to Level 10")
                        .icon(Key.key("iron_ingot"))
                        .condition(Condition.that(Variable.TOWN_LEVEL).greaterThan().value(9))
                        .child(level15 -> level15
                            .title("You Have Money")
                            .description("Level up your town to Level 15")
                            .icon(Key.key("gold_ingot"))
                            .condition(Condition.that(Variable.TOWN_LEVEL).greaterThan().value(14))
                            .child(level20 -> level20
                                .title("Now You're Just Bragging")
                                .description("Level up your town to Level 20")
                                .icon(Key.key("netherite_ingot"))
                                .frame(Frame.CHALLENGE)
                                .condition(Condition.that(Variable.TOWN_LEVEL).greaterThan().value(19))
                                .reward(Reward.of(Reward.Type.PLAYER_EXPERIENCE).quantity(500))
                            )
                        )
                    )
                )
            )
            .child(setBio -> setBio
                .title("A Brief History")
                .description("Set a bio for your town")
                .icon(Key.key("book"))
                .condition(Condition.that(Variable.TOWN_HAS_BIO).is().value(true))
                .child(changeColor -> changeColor
                    .title("Painting The Town Red, Green & Blue")
                    .description("Change your town's color")
                    .icon(Key.key("painting"))
                    .frame(Frame.GOAL)
                    .condition(Condition.that(Variable.TOWN_CHANGED_COLOR).greaterThan().value(0))
                )
            )
            .child(depositSmallAmount -> depositSmallAmount
                .title("A Penny Saved")
                .description("Deposit 100 into the town bank")
                .icon(Key.key("chainmail_helmet"))
                .condition(Condition.that(Variable.TOWN_MONEY).greaterThan().value(BigDecimal.valueOf(99)))
                .child(depositMediumAmount -> depositMediumAmount
                    .title("A Penny Earned")
                    .description("Deposit 5,000 into the town bank")
                    .icon(Key.key("golden_helmet"))
                    .condition(Condition.that(Variable.TOWN_MONEY).greaterThan().value(BigDecimal.valueOf(4999)))
                    .child(depositLargeAmount -> depositLargeAmount
                        .title("Interest Free")
                        .description("Deposit 15,000 into the town bank")
                        .icon(Key.key("turtle_helmet"))
                        .frame(Frame.CHALLENGE)
                        .condition(Condition.that(Variable.TOWN_MONEY).greaterThan().value(BigDecimal.valueOf(14999)))
                    )
                )
                .child(manyUniqueDepositors -> manyUniqueDepositors
                    .title("Tax Deductible")
                    .description("Have 10 unique players deposit into the town bank")
                    .icon(Key.key("emerald"))
                    .condition(Condition.that(Variable.TOWN_UNIQUE_DEPOSITORS).greaterThan().value(9))
                    .reward(Reward.of(Reward.Type.TOWN_MONEY).quantity(1000))
                    .child(manyUniqueDepositors2 -> manyUniqueDepositors2
                        .title("You Need An Auditor")
                        .description("Have 25 unique players deposit into the town bank")
                        .icon(Key.key("diamond"))
                        .frame(Frame.CHALLENGE)
                        .condition(Condition.that(Variable.TOWN_UNIQUE_DEPOSITORS).greaterThan().value(24))
                    )
                )
            )
        )
        .build();

    @Expose
    private String title;
    @Expose
    private String description;
    @Expose
    private String icon;
    @Expose
    private Frame frame;
    @Expose
    @SerializedName("send_notification")
    private boolean sendNotification = true;
    @Expose
    private List<Condition<?>> conditions;
    @Nullable
    @Expose
    private List<Reward> rewards;
    @Expose
    private List<Advancement> children;

    private Advancement(@NotNull String title, @NotNull String description, @Nullable Key icon, @Nullable Frame frame,
                        boolean sendNotification, @NotNull List<Condition<?>> conditions, @Nullable List<Reward> rewards,
                        @Nullable List<Advancement> children) {
        this.title = title;
        this.description = description;
        this.icon = icon == null ? Key.key("stone").asString() : icon.asString();
        this.frame = frame == null ? Frame.GOAL : frame;
        this.sendNotification = sendNotification;
        this.conditions = conditions;
        this.rewards = rewards;
        this.children = new ArrayList<>();
        if (children != null) {
            this.children.addAll(children);
        }
    }

    @SuppressWarnings("unused")
    private Advancement() {
        super();
    }

    @NotNull
    public String getTitle() {
        return title;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    @NotNull
    public String getIcon() {
        return icon;
    }

    @NotNull
    public Frame getFrame() {
        return frame;
    }

    public boolean doSendNotification() {
        return sendNotification;
    }

    @NotNull
    public List<Condition<?>> getConditions() {
        return Optional.ofNullable(conditions).orElse(List.of());
    }

    @NotNull
    public List<Advancement> getChildren() {
        return Optional.ofNullable(children).orElse(List.of());
    }

    public boolean areConditionsMet(@NotNull Town town, @NotNull OnlineUser user) {
        return getConditions().stream().allMatch(condition -> condition.isMet(town, user));
    }

    @NotNull
    public List<Reward> getRewards() {
        return Optional.ofNullable(rewards).orElse(List.of());
    }

    @NotNull
    public String getKey() {
        if (conditions.isEmpty()) {
            return title.toLowerCase(Locale.ENGLISH).replaceAll(" ", "_");
        }
        return conditions.stream()
            .map(Condition::getKey)
            .collect(Collectors.joining("_"));
    }

    public enum Frame {
        TASK,
        GOAL,
        CHALLENGE,
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String title;
        private String description = "";
        private Key icon = Key.key("stone");
        private Frame frame = Frame.TASK;
        private final List<Condition<?>> conditions = new ArrayList<>();
        private final List<Reward> rewards = new ArrayList<>();
        private boolean sendNotification = true;
        @Nullable
        private List<Advancement> children;

        private Builder() {
        }

        @NotNull
        public Builder title(@NotNull String title) {
            this.title = title;
            return this;
        }

        @NotNull
        public Builder description(@NotNull String description) {
            this.description = description;
            return this;
        }

        @NotNull
        public Builder icon(@NotNull Key icon) {
            this.icon = icon;
            return this;
        }

        @NotNull
        public Builder frame(@NotNull Frame frame) {
            this.frame = frame;
            return this;
        }

        @NotNull
        public Builder sendNotification(boolean sendNotification) {
            this.sendNotification = sendNotification;
            return this;
        }

        @NotNull
        public Builder child(@NotNull Function<Builder, Builder> child) {
            if (this.children == null) {
                this.children = new ArrayList<>();
            }
            this.children.add(child.apply(Advancement.builder()).build());
            return this;
        }

        @NotNull
        public <V> Builder condition(@NotNull Condition<V> condition) {
            this.conditions.add(condition);
            return this;
        }

        @NotNull
        public <V> Builder condition(@NotNull Condition.Builder<V> conditionBuilder) {
            return condition(conditionBuilder.build());
        }

        @NotNull
        public Builder reward(@NotNull Reward reward) {
            this.rewards.add(reward);
            return this;
        }

        @NotNull
        public Builder reward(@NotNull Reward.Builder rewardBuilder) {
            return reward(rewardBuilder.build());
        }

        @NotNull
        public Advancement build() {
            return new Advancement(title, description, icon, frame, sendNotification, conditions, rewards, children);
        }

    }

}
