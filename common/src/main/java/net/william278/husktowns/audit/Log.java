package net.william278.husktowns.audit;

import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

public class Log {

    private Map<LocalDateTime, Action> actions;

    private Log(@NotNull Map<LocalDateTime, Action> actions) {
        this.actions = actions;
    }

    @SuppressWarnings("unused")
    private Log() {
    }

    @NotNull
    public static Log newTownLog(@NotNull User creator) {
        final Log log = new Log(new TreeMap<>());
        log.log(Action.user(creator, Action.Type.CREATE_TOWN));
        return log;
    }

    public void log(@NotNull Action action) {
        this.actions.put(LocalDateTime.now(), action);
    }

}
