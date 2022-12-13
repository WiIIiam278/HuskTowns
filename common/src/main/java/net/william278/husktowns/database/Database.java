package net.william278.husktowns.database;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.config.Settings;
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
            final Settings.TableName table = Settings.TableName.match(matcher.group(1));
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

    public abstract Optional<User> getUser(@NotNull UUID uuid);

    public abstract void updateUser(@NotNull User user);

}
