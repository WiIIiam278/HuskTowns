package net.william278.husktowns.network;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

public class RedisBroker extends PluginMessageBroker {

    public RedisBroker(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    @Override
    public void initialize() throws RuntimeException {
        super.initialize();

        //todo redis init
    }

    @Override
    protected void send(@NotNull Message message, @NotNull OnlineUser sender) {
        // todo dispatch logic
    }

    @Override
    public void close() {
        super.close();

    }
}
