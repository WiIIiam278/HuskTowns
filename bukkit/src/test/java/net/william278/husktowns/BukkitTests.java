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
        awaitDatabaseOperations();

        // Check that the player's data is created
        Assertions.assertTrue(plugin.getDatabase().getUser(player.getUniqueId()).isPresent());
    }

    @Test
    public void testPlayerMakeTown() {
        // Create a player
        Player player = server.addPlayer();

        awaitDatabaseOperations();

        //todo broken because of casting on the default roles
        player.performCommand("husktowns:town create Testing");

        awaitDatabaseOperations();

        Assertions.assertTrue(plugin.getDatabase().getTown("Testing").isPresent());
    }

    @AfterAll
    public static void tearDownPlugin() {
        MockBukkit.unmock();
    }

    private void awaitDatabaseOperations() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
