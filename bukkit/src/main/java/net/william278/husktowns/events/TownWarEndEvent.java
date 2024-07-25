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

package net.william278.husktowns.events;

import net.william278.husktowns.town.Town;
import net.william278.husktowns.war.War;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TownWarEndEvent extends Event implements ITownWarEndEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Town attackingTown;
    private final Town defendingTown;
    private final War war;
    private final War.EndState endState;

    public TownWarEndEvent(@NotNull Town attackingTown, @NotNull Town defendingTown, @NotNull War war, @NotNull War.EndState endState) {
        this.attackingTown = attackingTown;
        this.defendingTown = defendingTown;
        this.war = war;
        this.endState = endState;
    }

    @Override
    @NotNull
    public Town getTownAttacking() {
        return attackingTown;
    }

    @Override
    @NotNull
    public Town getTownDefending() {
        return defendingTown;
    }

    @Override
    @NotNull
    public War getWar() {
        return war;
    }

    @Override
    @NotNull
    public War.EndState getWarEndReason() {
        return endState;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

}
