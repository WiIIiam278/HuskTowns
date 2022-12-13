package net.william278.husktowns.network;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

/**
 * <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/">Plugin Messaging channel</a> messenger implementation
 */
public class PluginMessageBroker extends Broker{

    /**
     * The name of BungeeCord's provided plugin channel.
     *
     * @implNote Technically, the effective identifier of this channel is {@code bungeecord:main},  but Spigot remaps
     * {@code BungeeCord} automatically to the new one (<a href="https://wiki.vg/Plugin_channels#bungeecord:main">source</a>).
     * Spigot's <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/">official documentation</a>
     * still instructs usage of {@code BungeeCord} as the name to use, however. It's all a bit inconsistent, so just in case
     * it's best to leave it how it is for to maintain backwards compatibility.
     */
    public static final String PLUGIN_CHANNEL_ID = "BungeeCord";

    protected PluginMessageBroker(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    @Override
    public void initialize() throws RuntimeException {

    }

    @Override
    protected void send(@NotNull Message message, @NotNull OnlineUser sender) {

    }

    @Override
    protected void changeServer(@NotNull OnlineUser user, @NotNull String server) {

    }
}
