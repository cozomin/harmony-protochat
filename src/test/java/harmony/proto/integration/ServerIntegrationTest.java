package harmony.proto.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import harmony.proto.dto.req.LoginReq;
import harmony.proto.dto.res.LoginRes;
import harmony.proto.server.WebSocketServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ServerIntegrationTest {

    private static Thread serverThread;
    private static final int TEST_PORT = 17575;
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void startServer() throws Exception {
        // Start the Netty server in a separate thread so it doesn't block the test
        serverThread = new Thread(() -> {
            try {
                WebSocketServer server = new WebSocketServer(TEST_PORT);
                server.start();
            } catch (Exception e) {
                System.err.println("Failed to start test server: " + e.getMessage());
            }
        });
        serverThread.start();

        Thread.sleep(3000);
    }

    @AfterAll
    public static void stopServer() {
        if (serverThread != null) {
            serverThread.interrupt();
        }
    }

    @Test
    public void testServerAcceptsConnectionAndParsesJSON() throws Exception {
        CompletableFuture<String> responseFuture = new CompletableFuture<>();

        // Create a real Java 11+ network client
        HttpClient client = HttpClient.newHttpClient();

        // Connect to the Netty server on TEST_PORT
        WebSocket webSocket = client.newWebSocketBuilder()
                .buildAsync(URI.create("ws://localhost:" + TEST_PORT + "/chat"), new WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        // When the server responds, save the JSON data
                        responseFuture.complete(data.toString());
                        return null;
                    }
                }).join();

        // Build a real request
        LoginReq req = new LoginReq("ghost_user", "wrong_pass");
        String jsonRequest = mapper.writeValueAsString(req);

        webSocket.sendText(jsonRequest, true).join();

        String jsonResponse = responseFuture.get(5, TimeUnit.SECONDS);

        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Test complete").join();

        assertNotNull(jsonResponse);
        assertTrue(jsonResponse.contains("LoginRes"));

        LoginRes res = mapper.readValue(jsonResponse, LoginRes.class);
        assertFalse(res.isSuccess());
    }
}