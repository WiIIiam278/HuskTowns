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

package net.william278.husktowns.command;

import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ChunkTabProvider extends TabProvider {

    @Override
    @NotNull
    default List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
        final Chunk chunk = user instanceof OnlineUser player ? player.getChunk() : Chunk.at(0, 0);
        return switch (args.length) {
            case 0, 1 -> filter(List.of(Integer.toString(chunk.getX())), args);
            case 2 -> filter(List.of(Integer.toString(chunk.getZ())), args);
            case 3 -> filter(getWorlds().stream().map(World::getName).toList(), args);
            default -> List.of();
        };
    }

    @NotNull
    List<World> getWorlds();

}
