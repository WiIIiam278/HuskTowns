package net.william278.husktowns.network;

import com.google.gson.annotations.Expose;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class Payload {
    @Nullable
    @Expose
    private UUID uuid;

    private Payload() {
    }

    @NotNull
    public static Payload uuid(@NotNull UUID uuid) {
        final Payload payload = new Payload();
        payload.uuid = uuid;
        return payload;
    }

    @NotNull
    public static Payload empty() {
        return new Payload();
    }

    public Optional<UUID> getUuid() {
        return Optional.ofNullable(uuid);
    }
}
