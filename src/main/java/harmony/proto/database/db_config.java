package harmony.proto.database;

public record db_config(String jdbcUrl, String username, String password, int maximumPoolSize) {

    public static db_config fromEnv() {
        return new db_config(
                getenv("DB_URL", "jdbc:postgresql://localhost:5432/harmony"),
                getenv("DB_USER", "postgres"),
                getenv("DB_PASSWORD", "SQLpa55"),
                Integer.parseInt(getenv("DB_POOL_SIZE", "10"))
        );
    }

    private static String getenv(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}