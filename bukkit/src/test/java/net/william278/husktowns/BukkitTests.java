package net.william278.husktowns;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.*;

public class BukkitTests {

    private static ServerMock server;
    private static BukkitHuskTowns plugin;

    @BeforeAll
    public static void setUpPlugin() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(BukkitHuskTowns.class);
    }

    @AfterAll
    public static void tearDownPlugin() {
        MockBukkit.unmock();
    }


}
