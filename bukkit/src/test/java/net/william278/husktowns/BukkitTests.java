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

    @Test
    public void testPlayerJoinDataIsCreated() {
        // Create a player
        Player player = server.addPlayer();

        // Wait 10 ms
        awaitDatabaseOperations(10);

        // Check that the player's data is created
        Assertions.assertTrue(plugin.getDatabase().getUser(player.getUniqueId()).isPresent());
    }

    @Test
    public void testPlayerMakeTown() {
        // Create a player
        Player player = server.addPlayer();

        awaitDatabaseOperations(10);

        player.performCommand("husktowns:town create Testing");

        awaitDatabaseOperations(40);

        Assertions.assertTrue(plugin.getDatabase().getTown("Testing").isPresent());
    }

    @AfterAll
    public static void tearDownPlugin() {
        MockBukkit.unmock();
    }

    private static void awaitDatabaseOperations(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
