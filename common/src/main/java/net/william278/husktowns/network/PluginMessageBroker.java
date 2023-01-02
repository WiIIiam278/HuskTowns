package net.william278.husktowns.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/">Plugin Messaging channel</a> messenger implementation
 */
public class PluginMessageBroker extends Broker {

    /**
     * The name of BungeeCord's provided plugin channel.
     *
     * @implNote Technically, the effective identifier of this channel is {@code bungeecord:main},  but Spigot remaps
     * {@code BungeeCord} automatically to the new one (<a href="https://wiki.vg/Plugin_channels#bungeecord:main">source</a>).
     * Spigot's <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/">official documentation</a>
     * still instructs usage of {@code BungeeCord} as the name to use, however. It's all a bit inconsistent, so just in case
     * it's best to leave it how it is for to maintain backwards compatibility.
     */
    public static final String BUNGEE_CHANNEL_ID = "BungeeCord";

    public PluginMessageBroker(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    @Override
    public void initialize() throws RuntimeException {
        plugin.initializePluginChannels();
    }

    @SuppressWarnings("UnstableApiUsage")
    public final void onReceive(@NotNull String channel, @NotNull OnlineUser user, byte[] message) {
        if (!channel.equals(BUNGEE_CHANNEL_ID)) {
            return;
        }

        final ByteArrayDataInput inputStream = ByteStreams.newDataInput(message);
        final String versionedKey = inputStream.readUTF();
        if (!versionedKey.equals(getVersionedKey())) {
            return;
        }

        short messageLength = inputStream.readShort();
        byte[] messageBytes = new byte[messageLength];
        inputStream.readFully(messageBytes);

        try (final DataInputStream readMessage = new DataInputStream(new ByteArrayInputStream(messageBytes))) {
            super.handle(user, plugin.getGson().fromJson(readMessage.readUTF(), Message.class));
        } catch (IOException e) {
            plugin.log(Level.SEVERE, "Failed to fully read plugin message", e);
        }
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    protected void send(@NotNull Message message, @NotNull OnlineUser sender) {
        final ByteArrayDataOutput outputStream = ByteStreams.newDataOutput();

        outputStream.writeUTF("ForwardToPlayer");
        outputStream.writeUTF(message.getTarget());
        outputStream.writeUTF(getVersionedKey());

        try (ByteArrayOutputStream messageOut = new ByteArrayOutputStream()) {
            messageOut.write(plugin.getGson().toJson(message).getBytes(StandardCharsets.UTF_8));
            outputStream.writeShort(messageOut.size());
            outputStream.write(messageOut.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize message", e);
        }

        sender.sendPluginMessage(BUNGEE_CHANNEL_ID, outputStream.toByteArray());
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void changeServer(@NotNull OnlineUser user, @NotNull String server) {
        final ByteArrayDataOutput outputStream = ByteStreams.newDataOutput();

        outputStream.writeUTF("Connect");
        outputStream.writeUTF(server);

        user.sendPluginMessage(BUNGEE_CHANNEL_ID, outputStream.toByteArray());
    }

    @Override
    public void close() {
    }

    @NotNull
    private String getVersionedKey() {
        final String version = plugin.getVersion().getMajor() + "." + plugin.getVersion().getMinor();
        return plugin.getKey(plugin.getSettings().clusterId, version).asString();
    }
}
