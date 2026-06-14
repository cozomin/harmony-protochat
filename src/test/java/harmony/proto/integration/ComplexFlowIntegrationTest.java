package harmony.proto.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.database.connection_manager;
import harmony.proto.dto.MessageDTO;
import harmony.proto.server.WebSocketServer;
import harmony.proto.server.WebSocketServerHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class ComplexFlowIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("harmony_complex")
            .withUsername("test")
            .withPassword("test");

    private static HikariDataSource dataSource;
    private static Thread serverThread;
    private static final int TEST_PORT = 17576;
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeAll
    public static void setupEnvironment() throws Exception {
        WebSocketServerHandler.clearOnlineUsers();

        // Force the actual WebSocketClient to connect to the local test server port
        System.setProperty("url", "ws://127.0.0.1:" + TEST_PORT + "/chat");

        // Start the Netty server
        serverThread = new Thread(() -> {
            try {
                WebSocketServer server = new WebSocketServer(TEST_PORT);
                server.start();
            } catch (Exception e) {
                // InterruptedException is normal upon teardown
            }
        });
        serverThread.start();

        Thread.sleep(3500);

        // Configure the test DB container
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl() + "&stringtype=unspecified");
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        dataSource = new HikariDataSource(config);

        // Inject the test DB into the running server
        injectGlobalDataSource(dataSource);

        // Create the schema to cover DAO variations
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE hm_user (username VARCHAR(50) PRIMARY KEY, pass VARCHAR(100) NOT NULL);");
            stmt.execute("CREATE TABLE chat (chatID SERIAL PRIMARY KEY, chatName VARCHAR(50), isGroup BOOLEAN, updated_at TIMESTAMP);");

            // Supporting variations for member naming (both memberID and username)
            stmt.execute("CREATE TABLE chat_member (chatID INT, memberID VARCHAR(50), username VARCHAR(50), hm_role VARCHAR(20));");

            stmt.execute("CREATE TYPE mess_enum AS ENUM ('regular', 'system', 'file');");

            // Supporting variations for message sender naming (both senderID and username)
            stmt.execute("CREATE TABLE hm_message (messID SERIAL PRIMARY KEY, senderID VARCHAR(50), username VARCHAR(50), chatID INT, message_content TEXT, sent_at TIMESTAMP, message_type mess_enum);");

            // Seed test users cos and link
            String hashedPass = BCrypt.hashpw("secret", BCrypt.gensalt());
            stmt.execute("INSERT INTO hm_user VALUES ('cos', '" + hashedPass + "'), ('link', '" + hashedPass + "');");

            // Seed test chat (Chat ID 1)
            stmt.execute("INSERT INTO chat VALUES (1, 'DM', false, CURRENT_TIMESTAMP);");
            stmt.execute("INSERT INTO chat_member (chatID, memberID, hm_role) VALUES (1, 'cos', 'member'), (1, 'link', 'member');");
        } catch (Exception e) {
            System.err.println("SQL Setup Error: " + e.getMessage());
            throw e;
        }
    }

    @AfterAll
    public static void teardown() {
        if (serverThread != null) serverThread.interrupt();
        if (dataSource != null) dataSource.close();
    }

    @Test
    public void testFullEndToEndMessageRoutingAndPersistence() throws Exception {
        System.out.println("--- STARTING E2E TEST ---");

        // Initialize the actual clients
        WebSocketClient cosClient = new WebSocketClient();
        WebSocketClient linkClient = new WebSocketClient();

        // Perform real login
        assertTrue(cosClient.login("cos", "secret"), "cos failed to login");
        assertTrue(linkClient.login("link", "secret"), "link failed to login");

        System.out.println("--- BOTH CLIENTS LOGGED IN ---");

        // Allow some time for server to stabilize the onlineUsers map
        Thread.sleep(1000);

        // Setup an interceptor to catch the live message on link's client
        CompletableFuture<MessageDTO> receivedMessageFuture = new CompletableFuture<>();

        linkClient.setLiveMessageListener(message -> {
            if (message.getContent() != null && message.getContent().contains("Hello link")) {
                receivedMessageFuture.complete(message);
            }
        });

        System.out.println("--- cos SENDS MESSAGE ---");
        // Send the message using the actual method
        cosClient.sendMessage("Hello link, this is an E2E test!", 1L);

        System.out.println("--- link IS WAITING FOR MESSAGE ---");
        MessageDTO receivedMessage;
        try {
            // Block the test thread until the LiveMessageListener triggers
            receivedMessage = receivedMessageFuture.get(10, TimeUnit.SECONDS);
            assertNotNull(receivedMessage, "link should have received the broadcasted message");
        } catch (Exception e) {
            System.err.println("CRITICAL FAILURE: Server did not broadcast message to link.");
            throw e;
        }

        // Assert the data decoded by the client is perfectly intact
        assertEquals("cos", receivedMessage.getSenderId());
        assertEquals("Hello link, this is an E2E test!", receivedMessage.getContent());

        // Verify persistence in the PostgreSQL database
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM hm_message WHERE chatID = 1")) {
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next(), "The message should exist in the PostgreSQL database");
            assertEquals("Hello link, this is an E2E test!", rs.getString("message_content"));
            assertEquals("regular", rs.getString("message_type"));
        }

        cosClient.disconnect();
        linkClient.disconnect();
        System.out.println("--- TEST COMPLETED SUCCESSFULLY ---");
    }

    private static void injectGlobalDataSource(HikariDataSource mockDataSource) {
        try {
            for (Field field : connection_manager.class.getDeclaredFields()) {
                if (HikariDataSource.class.isAssignableFrom(field.getType()) && Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    field.set(null, mockDataSource);
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject DB", e);
        }
    }
}