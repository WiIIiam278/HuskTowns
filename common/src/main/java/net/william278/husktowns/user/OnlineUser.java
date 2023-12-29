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

package net.william278.husktowns.user;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import net.william278.cloplib.operation.OperationUser;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.config.Locales;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class OnlineUser extends User implements CommandUser, OperationUser {

    @NotNull
    protected final HuskTowns plugin;

    protected OnlineUser(@NotNull UUID uuid, @NotNull String username, @NotNull HuskTowns plugin) {
        super(uuid, username);
        this.plugin = plugin;
    }

    @NotNull
    public abstract Chunk getChunk();

    @NotNull
    public abstract Position getPosition();

    @NotNull
    public abstract World getWorld();

    public abstract void sendPluginMessage(@NotNull String channel, byte[] message);

    public final void sendActionBar(@NotNull Component component) {
        getAudience().sendActionBar(component);
    }

    public final void sendActionBar(@NotNull MineDown mineDown) {
        this.sendActionBar(mineDown.toComponent());
    }

    public final void sendTitle(@NotNull Component title, @NotNull Component subtitle) {
        getAudience().showTitle(Title.title(title, subtitle));
    }

    public final void sendMessage(@NotNull Locales.Slot slot, @NotNull Component message) {
        switch (slot) {
            case CHAT -> this.sendMessage(message);
            case ACTION_BAR -> this.sendActionBar(message);
            case TITLE -> this.sendTitle(message, Component.empty());
            case SUBTITLE -> this.sendTitle(Component.empty(), message);
        }
    }

    public final void sendMessage(@NotNull Locales.Slot slot, @NotNull MineDown mineDown) {
        this.sendMessage(slot, mineDown.toComponent());
    }

    public final void playSound(@Subst("minecraft:block.note_block.banjo") @NotNull String sound) {
        getAudience().playSound(Sound.sound(Key.key(sound), Sound.Source.PLAYER, 1.0f, 1.0f));
    }

    @NotNull
    public Audience getAudience() {
        return plugin.getAudience(getUuid());
    }

    public abstract boolean isSneaking();

    public abstract void spawnMarkerParticle(@NotNull Position position, @NotNull TextColor color, int count);

    public abstract void teleportTo(@NotNull Position position);

    public abstract void giveExperiencePoints(int quantity);

    public abstract void giveExperienceLevels(int quantity);

    public abstract void giveItem(@NotNull Key material, int quantity);

}
