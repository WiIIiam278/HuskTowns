package net.william278.husktowns.user;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class OnlineUser extends User {

    protected OnlineUser(@NotNull UUID uuid, @NotNull String username) {
        super(uuid, username);
    }

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
