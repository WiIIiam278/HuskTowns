package net.william278.husktowns.visualizer;

import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

public interface Visualizer {

    void highlightChunks(@NotNull OnlineUser user, @NotNull List<Chunk> chunks, @NotNull Color color);

    void highlightChunk(@NotNull OnlineUser user, @NotNull Chunk chunk, @NotNull Color color);

}
