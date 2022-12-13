package net.william278.husktowns.user;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.World;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class OnlineUser extends User {

    protected OnlineUser(@NotNull UUID uuid, @NotNull String username) {
        super(uuid, username);
    }

    @NotNull
    public abstract Position getChunkPosition();

    @NotNull
    public abstract World getWorld();

    public abstract void sendPluginMessage(@NotNull String channel, byte[] message);

    public final void sendMessage(@NotNull MineDown mineDown) {
        getAudience().sendMessage(mineDown.toComponent());
    }

    public final void sendActionBar(@NotNull MineDown mineDown) {
        getAudience().sendActionBar(mineDown.toComponent());
    }

    public final void playSound(@Subst("minecraft:block.note_block.banjo") @NotNull String sound) {
        getAudience().playSound(Sound.sound(Key.key(sound), Sound.Source.PLAYER, 1.0f, 1.0f));
    }

    protected abstract Audience getAudience();

}
