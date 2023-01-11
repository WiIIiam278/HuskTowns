package net.william278.husktowns.audit;

import com.google.gson.annotations.Expose;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.TreeMap;

public class Log {

    @Expose
    private Map<OffsetDateTime, Action> actions;

    private Log(@NotNull Map<OffsetDateTime, Action> actions) {
        this.actions = actions;
    }

    @SuppressWarnings("unused")
    private Log() {
    }

    @NotNull
    public static Log newTownLog(@NotNull User creator) {
        final Log log = new Log(new TreeMap<>());
        log.log(Action.of(creator, Action.Type.CREATE_TOWN));
        return log;
    }

    @NotNull
    public static Log empty() {
        return new Log(new TreeMap<>());
    }

    @NotNull
    public static Log migratedLog(@NotNull OffsetDateTime foundedTime) {
        final Log log = new Log(new TreeMap<>());
        log.actions.put(foundedTime, Action.of(Action.Type.CREATE_TOWN));
        log.log(Action.of(Action.Type.TOWN_DATA_MIGRATED));
        return log;
    }

    public void log(@NotNull Action action) {
        this.actions.put(OffsetDateTime.now(), action);
    }

    @NotNull
    public Map<OffsetDateTime, Action> getActions() {
        return actions;
    }

    /**
     * Returns when the town was founded
     *
     * @return the {@link OffsetDateTime} of the first {@link Action.Type#CREATE_TOWN}
     */
    @NotNull
    public OffsetDateTime getFoundedTime() {
        return actions.entrySet().stream()
                .filter(entry -> entry.getValue().getAction() == Action.Type.CREATE_TOWN)
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(OffsetDateTime.now());
    }

}
