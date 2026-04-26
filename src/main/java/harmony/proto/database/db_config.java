package harmony.proto.database;

public final class db_config {
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final int maximumPoolSize;

    public db_config(String jdbcUrl, String username, String password, int maximumPoolSize) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.maximumPoolSize = maximumPoolSize;
    }

    public static db_config fromEnv() {
        return new db_config(
                getenv("DB_URL", "jdbc:postgresql://localhost:5432/harmony"),
                getenv("DB_USER", "postgres"),
                getenv("DB_PASSWORD", "postgres"),
                Integer.parseInt(getenv("DB_POOL_SIZE", "10"))
        );
    }

    private static String getenv(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public String getJdbcUrl() { return jdbcUrl; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getMaximumPoolSize() { return maximumPoolSize; }
}