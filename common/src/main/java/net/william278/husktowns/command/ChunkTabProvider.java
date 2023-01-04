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
