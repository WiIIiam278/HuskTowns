package me.william278.husktowns.data.sql;

import me.william278.husktowns.HuskTowns;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class Database {
    protected HuskTowns plugin;

    public final static String DATA_POOL_NAME = "HuskTownsHikariPool";

    public Database(HuskTowns instance) {
        plugin = instance;
    }

    public abstract Connection getConnection() throws SQLException;

    public abstract void load();

    public abstract void backup();

    public abstract void close();

    public final int hikariMaximumPoolSize = HuskTowns.getSettings().getHikariMaximumPoolSize();
    public final int hikariMinimumIdle = HuskTowns.getSettings().getHikariMinimumIdle();
    public final long hikariMaximumLifetime = HuskTowns.getSettings().getHikariMaximumLifetime();
    public final long hikariKeepAliveTime = HuskTowns.getSettings().getHikariKeepAliveTime();
    public final long hikariConnectionTimeOut = HuskTowns.getSettings().getHikariConnectionTimeOut();
}
