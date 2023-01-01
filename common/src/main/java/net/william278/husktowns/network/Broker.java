package net.william278.husktowns.network;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Role;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.SavedUser;
import net.william278.husktowns.user.User;
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
                        plugin.getManager().sendTownNotification(town, plugin.getLocales()
                                .getLocale("town_deleted_notification")
                                .map(MineDown::toComponent).orElse(Component.empty()));
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
            case TOWN_LEVEL_UP, TOWN_TRANSFERRED, TOWN_RENAMED ->
                    message.getPayload().getInteger().flatMap(id -> plugin.getTowns().stream()
                            .filter(town -> town.getId() == id).findFirst()).ifPresent(town -> {
                        final Component locale = switch (message.getType()) {
                            case TOWN_LEVEL_UP -> plugin.getLocales().getLocale("town_levelled_up",
                                    Long.toString(town.getLevel())).map(MineDown::toComponent).orElse(Component.empty());
                            case TOWN_RENAMED -> plugin.getLocales().getLocale("town_renamed",
                                    town.getName()).map(MineDown::toComponent).orElse(Component.empty());
                            case TOWN_TRANSFERRED -> plugin.getLocales().getLocale("town_transferred",
                                            town.getName(), plugin.getDatabase().getUser(town.getMayor())
                                                    .map(SavedUser::user).map(User::getUsername).orElse("?"))
                                    .map(MineDown::toComponent).orElse(Component.empty());
                            default -> Component.empty();
                        };
                        plugin.getManager().sendTownNotification(town, locale);
                    });
            case TOWN_DEMOTED, TOWN_PROMOTED, TOWN_EVICTED -> {
                message.getPayload().getInteger().flatMap(id -> plugin.getTowns().stream()
                        .filter(town -> town.getId() == id).findFirst()).ifPresent(town -> {
                    final Component locale = switch (message.getType()) {
                        case TOWN_DEMOTED -> plugin.getLocales().getLocale("demoted_you",
                                        plugin.getUserTown(receiver).map(Member::role).map(Role::getName).orElse("?"),
                                        message.getSender()).map(MineDown::toComponent)
                                .orElse(Component.empty());
                        case TOWN_PROMOTED -> plugin.getLocales().getLocale("promoted_you",
                                        plugin.getUserTown(receiver).map(Member::role).map(Role::getName).orElse("?"),
                                        message.getSender()).map(MineDown::toComponent)
                                .orElse(Component.empty());
                        case TOWN_EVICTED -> plugin.getLocales().getLocale("evicted_you",
                                        town.getName(), message.getSender()).map(MineDown::toComponent)
                                .orElse(Component.empty());
                        default -> Component.empty();
                    };
                    receiver.sendMessage(locale);
                });
            }
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
