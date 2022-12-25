package net.william278.husktowns.user;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.World;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.UUID;

public abstract class OnlineUser extends User implements CommandUser {

    protected OnlineUser(@NotNull UUID uuid, @NotNull String username) {
        super(uuid, username);
    }

    @NotNull
    public abstract Chunk getChunk();

    @NotNull
    public abstract Position getPosition();

    @NotNull
    public abstract World getWorld();

    public abstract void sendPluginMessage(@NotNull String channel, byte[] message);

    public final void sendActionBar(@NotNull MineDown mineDown) {
        getAudience().sendActionBar(mineDown.toComponent());
    }

    public final void playSound(@Subst("minecraft:block.note_block.banjo") @NotNull String sound) {
        getAudience().playSound(Sound.sound(Key.key(sound), Sound.Source.PLAYER, 1.0f, 1.0f));
    }

    public abstract void spawnMarkerParticle(@NotNull Position position, @NotNull Color color, int count);

    @NotNull
    public abstract Audience getAudience();

}
