package net.william278.husktowns.network;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Payload {
    @Nullable
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

}
