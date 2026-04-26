package harmony.proto.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public final class connection_manager {
    private static HikariDataSource dataSource;

    public connection_manager() {}

    public static void init(db_config config) {
        if (dataSource != null) {
            return;
        }

        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(config.getJdbcUrl());
        hikari.setUsername(config.getUsername());
        hikari.setPassword(config.getPassword());
        hikari.setDriverClassName("org.postgresql.Driver");
        hikari.setMaximumPoolSize(config.getMaximumPoolSize());
        hikari.setMinimumIdle(2);
        hikari.setConnectionTimeout(10000);
        hikari.setIdleTimeout(600000);
        hikari.setMaxLifetime(1800000);
        hikari.setAutoCommit(true);
        hikari.setPoolName("harmony-hikari-pool");

        dataSource = new HikariDataSource(hikari);
    }

    public static DataSource getDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("ConnectionManager not initialized.");
        }
        return dataSource;
    }

    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}