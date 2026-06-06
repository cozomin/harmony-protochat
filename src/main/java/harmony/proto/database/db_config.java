package harmony.proto.database;

public record db_config(String jdbcUrl, String username, String password, int maximumPoolSize) {

    public static db_config fromEnv() {
        return new db_config(
                getenv("DB_URL", "fake"),
                getenv("DB_USER", "fake"),
                getenv("DB_PASSWORD", "fake"),
                Integer.parseInt(getenv("DB_POOL_SIZE", "10"))
        );
    }

    private static String getenv(String key, String defaultValue) {
        String value = System.getenv(key); // gets the credentials from the local .env file
        //!! THE FILE MUST BE ADDED TO THE ENVIROMENT VARIABLES FIELD IN THE SERVER CONFIGURATIONS TAB WITH THE ENV EXTENTION INSTALLED
        return value == null || value.isBlank() ? defaultValue : value;
    }
}