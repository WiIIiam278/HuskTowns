package net.william278.husktowns.database;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Database {

    protected final HuskTowns plugin;
    private final String schemaFile;

    protected Database(@NotNull HuskTowns plugin, @NotNull String schemaFile) {
        this.plugin = plugin;
        this.schemaFile = "database/" + schemaFile;
    }

    /**
     * Get the schema statements from the schema file
     *
     * @return the {@link #format formatted} schema statements
     */
    @NotNull
    protected final String[] getSchema() {
        try (InputStream schemaStream = Objects.requireNonNull(plugin.getResource(schemaFile))) {
            final String schema = new String(schemaStream.readAllBytes(), StandardCharsets.UTF_8);
            return format(schema).split(";");
        } catch (IOException e) {
            plugin.log(Level.SEVERE, "Failed to load database schema", e);
        }
        return new String[0];
    }

    /**
     * Format a string for use in a SQL query
     *
     * @param statement The SQL statement to format
     * @return The formatted SQL statement
     */
    @NotNull
    protected final String format(@NotNull String statement) {
        final Pattern pattern = Pattern.compile("%(\\w+)%");
        final Matcher matcher = pattern.matcher(statement);
        final StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            final Table table = Table.match(matcher.group(1));
            matcher.appendReplacement(sb, plugin.getSettings().getTableName(table));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Initialize the database connection
     *
     * @throws RuntimeException if the database initialization fails
     */
    public abstract void initialize() throws RuntimeException;

    /**
     * Get a user by their UUID
     *
     * @param uuid The UUID of the user
     * @return The user, if they exist
     */
    public abstract Optional<User> getUser(@NotNull UUID uuid);

    /**
     * Get a user by their name
     *
     * @param username The name of the user
     * @return The user, if they exist
     */
    public abstract Optional<User> getUser(@NotNull String username);

    /**
     * Add a user to the database
     *
     * @param user The user to add
     */
    public abstract void createUser(@NotNull User user);

    /**
     * Update a user's name in the database
     *
     * @param user The user to update
     */
    public abstract void updateUser(@NotNull User user);

    /**
     * Get a town by its UUID
     *
     * @param townUuid The UUID of the town
     * @return The town, if it exists
     */
    public abstract Optional<Town> getTown(@NotNull UUID townUuid);

    /**
     * Get a town by its name
     *
     * @param townName The name of the town
     * @return The town, if it exists
     */
    public abstract Optional<Town> getTown(@NotNull String townName);

    /**
     * Add a town to the database
     *
     * @param town The town to add
     */
    protected abstract void createTown(@NotNull Town town);

    /**
     * Update a town's name in the database
     *
     * @param town The town to update
     */
    public abstract void updateTown(@NotNull Town town);

    /**
     * Delete a town from the database
     *
     * @param townUuid The UUID of the town to delete
     */
    public abstract void deleteTown(@NotNull UUID townUuid);

    /**
     * Get a claim world on a server
     *
     * @param world  The world to get the claim world for
     * @param server The server to get the claim world for
     * @return The claim world, if it exists
     */
    public abstract Optional<ClaimWorld> getClaimWorld(@NotNull World world, @NotNull String server);

    /**
     * Create a new claim world and add it to the database
     *
     * @param world The world to create the claim world for
     * @return The created claim world
     */
    @NotNull
    public abstract ClaimWorld createClaimWorld(@NotNull World world);

    /**
     * Update a claim world in the database
     *
     * @param claimWorld The claim world to update
     */
    public abstract void updateClaimWorld(@NotNull ClaimWorld claimWorld);

    /**
     * Get a member by their UUID
     *
     * @param world  The world to get the member for
     * @param server The server to get the member for
     */
    public abstract void deleteClaimWorld(@NotNull World world, @NotNull String server);

    /**
     * Get a user's town membership data
     *
     * @param userUuid The user to get the membership data for
     * @return The user's membership data, if they are a member of a town
     */
    public abstract Optional<Member> getMember(@NotNull UUID userUuid);

    /**
     * Save membership data to the database for a user
     *
     * @param member The member to save
     */
    public abstract void createMember(@NotNull Member member);

    /**
     * Update a user's membership data in the database
     *
     * @param member The member to update
     */
    public abstract void updateMember(@NotNull Member member);

    /**
     * Delete a user's membership data from the database
     *
     * @param userUuid The user to delete the membership data for
     */
    public abstract void deleteMember(@NotNull UUID userUuid);


    /**
     * Identifies types of databases
     */
    public enum Type {
        MYSQL("MySQL"),
        SQLITE("SQLite");
        @NotNull
        public final String displayName;

        Type(@NotNull String displayName) {
            this.displayName = displayName;
        }
    }

    /**
     * Represents the names of tables in the database
     */
    public enum Table {
        USER_DATA("husktowns_users"),
        TOWN_DATA("husktowns_towns"),
        MEMBER_DATA("husktowns_members"),
        CLAIM_DATA("husktowns_claims");
        @NotNull
        public final String defaultName;

        Table(@NotNull String defaultName) {
            this.defaultName = defaultName;
        }

        @NotNull
        public static Database.Table match(@NotNull String placeholder) throws IllegalArgumentException {
            return Table.valueOf(placeholder.toUpperCase());
        }
    }
}
