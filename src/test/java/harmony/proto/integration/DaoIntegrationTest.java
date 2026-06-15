package harmony.proto.integration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import harmony.proto.dao.UserDao;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class DaoIntegrationTest {

    // Testcontainers will download and run a real, isolated PostgreSQL instance
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("harmony_test")
            .withUsername("test")
            .withPassword("test");

    private static HikariDataSource dataSource;
    private UserDao userDao;

    @BeforeAll
    public static void setupDatabase() throws Exception {
        // Configure HikariCP to connect to the test container
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        dataSource = new HikariDataSource(config);

        // Create the temporary database schema (only the tables needed for the test)
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE hm_user (" +
                    "username VARCHAR(50) PRIMARY KEY, " +
                    "pass VARCHAR(100) NOT NULL" +
                    ");");

            //  can also add CREATE TABLE for chat, chat_member, etc
        }
    }

    @AfterAll
    public static void teardown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @BeforeEach
    public void setup() {
        userDao = new UserDao(dataSource);
    }

    @Test
    public void testSignUpAndLoginIntegration() throws Exception {
        // Test registration (real INSERT into DB)
        String rawPassword = "mySecretPassword";
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        String signedUpUser = userDao.signUp("integration_user", hashedPassword);
        assertEquals("integration_user", signedUpUser, "The user should be created successfully");

        // Check if it exists in the real database (SELECT)
        assertTrue(userDao.existsByUsername("integration_user"));
        assertFalse(userDao.existsByUsername("ghost_user"));

        // Test login with the correct password
        String loggedInUser = userDao.login("integration_user", rawPassword);
        assertEquals("integration_user", loggedInUser, "Login should be successful with the correct password");

        // Test login with the wrong password
        String failedLogin = userDao.login("integration_user", "wrongPassword");
        assertNull(failedLogin, "Login should fail with the wrong password");
    }
}