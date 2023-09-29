package net.william278.husktowns.util;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface GlobalUserList {

    @NotNull
    Map<String, List<User>> getGlobalUserList();

    default List<User> getUserList() {
        return Stream.concat(
                getGlobalUserList().values().stream().flatMap(Collection::stream),
                getPlugin().getOnlineUsers().stream()
        ).distinct().sorted().toList();
    }

    default void setUserList(@NotNull String server, @NotNull List<User> players) {
        getGlobalUserList().values().forEach(list -> {
            list.removeAll(players);
            list.removeAll(getPlugin().getOnlineUsers());
        });
        getGlobalUserList().put(server, players);
    }

    @NotNull
    @ApiStatus.Internal
    HuskTowns getPlugin();

}
