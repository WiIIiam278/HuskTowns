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
        final Player player = server.addPlayer();
        awaitDatabaseOperations(10);
        Assertions.assertTrue(plugin.getDatabase().getUser(player.getUniqueId()).isPresent());
    }

    @Test
    public void testPlayerMakeTown() {
        final Player player = server.addPlayer();
        awaitDatabaseOperations(10);
        player.performCommand("husktowns:town create Testing");
        awaitDatabaseOperations(40);
        Assertions.assertTrue(plugin.getDatabase().getTown("Testing").isPresent());
    }

    @Test
    public void testPlayerMakeTownAndClaim() {
        final Player player = server.addPlayer();
        awaitDatabaseOperations(10);
        player.performCommand("husktowns:town create TestClaims");
        awaitDatabaseOperations(40);
        player.performCommand("husktowns:town claim");
        awaitDatabaseOperations(40);
        Assertions.assertTrue(plugin.getDatabase().getTown("TestClaims").isPresent());
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
