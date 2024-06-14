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

package net.william278.husktowns.hook;

import net.william278.cloplib.operation.Operation;
import net.william278.cloplib.operation.OperationType;
import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.event.HomeCreateEvent;
import net.william278.huskhomes.event.HomeEditEvent;
import net.william278.huskhomes.teleport.TeleportBuilder;
import net.william278.huskhomes.user.CommandUser;
import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.logging.Level;

public class HuskHomesHook extends TeleportationHook implements Listener {
    @Nullable
    private HuskHomesAPI api;
    @PluginHook(id = "HuskHomes", register = PluginHook.Register.ON_ENABLE, platform = "bukkit")
    public HuskHomesHook(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        this.api = HuskHomesAPI.getInstance();
        Bukkit.getPluginManager().registerEvents(this, (BukkitHuskTowns) plugin);
        plugin.log(Level.INFO, "Enabled HuskHomes teleportation hook");
    }

    @Override
    public void teleport(@NotNull OnlineUser user, @NotNull Position position, @NotNull String server,
                         boolean instant) {
        try {
            final HuskHomesAPI api = getHuskHomes()
                    .orElseThrow(() -> new IllegalStateException("HuskHomes API not present"));
            final TeleportBuilder builder = api
                    .teleportBuilder(api.adaptUser(((BukkitUser) user).getPlayer()))
                    .target(net.william278.huskhomes.position.Position.at(
                            position.getX(),
                            position.getY(),
                            position.getZ(),
                            position.getYaw(),
                            position.getPitch(),
                            net.william278.huskhomes.position.World.from(position.getWorld().getName(), position.getWorld().getUuid()),
                            server
                    ));
            if (instant) {
                builder.toTeleport().execute();
            } else {
                builder.toTimedTeleport().execute();
            }
        } catch (IllegalStateException e) {
            plugin.getLocales().getLocale("error_town_spawn_not_set")
                    .ifPresent(user::sendMessage);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSetHome(@NotNull HomeCreateEvent event) {
        if (this.cancelEvent(event.getCreator(), event.getPosition())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRelocateHome(@NotNull HomeEditEvent event) {
        if (this.cancelEvent(event.getEditor(), event.getHome())) {
            event.setCancelled(true);
        }
    }

    private boolean cancelEvent(@NotNull CommandUser creator, @NotNull net.william278.huskhomes.position.Position home) {
        final Optional<? extends OnlineUser> user = creator instanceof net.william278.huskhomes.user.OnlineUser online ?
                plugin.getOnlineUsers().stream().filter(u -> u.getUuid().equals(online.getUuid())).findFirst() :
                Optional.empty();
        if (user.isEmpty()) {
            return false;
        }

        final net.william278.huskhomes.position.World world = home.getWorld();
        final Position position = Position.at(home.getX(), home.getY(), home.getZ(),
                World.of(world.getUuid(), world.getName(), world.getEnvironment().name()),
                home.getYaw(), home.getPitch());

        return plugin.cancelOperation(Operation
                .of(user.get(), OperationType.BLOCK_INTERACT, position));
    }

    private Optional<HuskHomesAPI> getHuskHomes() {
        return Optional.ofNullable(api);
    }

}
