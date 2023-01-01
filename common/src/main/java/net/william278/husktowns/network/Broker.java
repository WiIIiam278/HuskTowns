package net.william278.husktowns.network;

import de.themoep.minedown.adventure.MineDown;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class Broker {

    protected final HuskTowns plugin;

    protected Broker(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
    }

    protected void handle(@NotNull OnlineUser receiver, @NotNull Message message) {
        switch (message.getType()) {
            case TOWN_DELETE -> message.getPayload().getInteger()
                    .flatMap(townId -> plugin.getTowns().stream().filter(town -> town.getId() == townId).findFirst())
                    .ifPresent(town -> plugin.runAsync(() -> {
                        plugin.getTowns().remove(town);
                        plugin.getClaimWorlds().values().forEach(world -> {
                            if (world.removeTownClaims(town.getId()) > 0) {
                                plugin.getDatabase().updateClaimWorld(world);
                            }
                        });
                    }));
            case TOWN_UPDATE -> message.getPayload().getInteger()
                    .ifPresent(townId -> plugin.runAsync(() -> plugin.getDatabase().getTown(townId)
                            .ifPresent(town -> plugin.getTowns().replaceAll(t -> t.getId() == town.getId() ? town : t))));
            case TOWN_INVITE_REQUEST -> message.getPayload().getInvite()
                    .ifPresent(invite -> plugin.getManager().towns().handleInboundInvite(receiver, invite));
            case TOWN_INVITE_REPLY -> message.getPayload().getBool().ifPresent(accepted -> {
                final Optional<Member> member = plugin.getUserTown(receiver);
                if (member.isEmpty()) {
                    return;
                }
                final Member townMember = member.get();
                if (!accepted) {
                    plugin.getLocales().getLocale("invite_declined_by", message.getSender())
                            .ifPresent(receiver::sendMessage);
                    return;
                }
                plugin.getLocales().getLocale("user_joined_town", message.getSender(),
                                townMember.town().getName()).map(MineDown::toComponent)
                        .ifPresent(locale -> plugin.getManager().sendTownNotification(townMember.town(), locale));
            });
            case TOWN_USER_EVICTED -> message.getPayload().getString().ifPresent(locale -> {
                //todo Send eviction message to player w/ UUID on this server
            });
            case TOWN_CHAT_MESSAGE -> message.getPayload().getString()
                    .ifPresent(text -> plugin.getDatabase().getUser(message.getSender())
                            .flatMap(sender -> plugin.getUserTown(sender.user())).ifPresent(member -> {
                                final Town town = member.town();
                                plugin.getLocales().getLocale("town_chat_message_format",
                                                town.getName(), town.getColorRgb(), member.user().getUsername(),
                                                member.role().getName(), text)
                                        .map(MineDown::toComponent)
                                        .ifPresent(locale -> plugin.getManager().sendTownNotification(town, locale));
                            }));

        }
    }

    public abstract void initialize() throws RuntimeException;

    protected abstract void send(@NotNull Message message, @NotNull OnlineUser sender);

    protected abstract void changeServer(@NotNull OnlineUser user, @NotNull String server);

    public abstract void close();

    /**
     * Identifies types of message brokers
     */
    public enum Type {
        PLUGIN_MESSAGE("Plugin Messages"),
        REDIS("Redis");
        @NotNull
        public final String displayName;

        Type(@NotNull String displayName) {
            this.displayName = displayName;
        }
    }

}
