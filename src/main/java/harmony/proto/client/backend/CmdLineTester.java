package harmony.proto.client.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import harmony.proto.dto.MessageDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Instant;
import java.util.Scanner;
import java.util.concurrent.CompletionStage;

public class CmdLineTester {

    private static final String SERVER_URL = "ws://127.0.0.1:7575/chat";
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public static void main(String[] args) {
        System.out.println("Starting CLI Tester...");

        // 1. Build the WebSocket Client
        HttpClient client = HttpClient.newHttpClient();
        WebSocket webSocket = client.newWebSocketBuilder()
                .buildAsync(URI.create(SERVER_URL), new WebSocketListener())
                .join();

        System.out.println("Connected to: " + SERVER_URL);
        System.out.println("Type your message in this format: [senderId] [chatId] [your message here]");
        System.out.println("Example: 4 1 Hello World!");
        System.out.println("Type 'exit' to quit.\n");

        // 2. Read from Command Line
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();

            if ("exit".equalsIgnoreCase(input.trim())) {
                break;
            }

            try {
                // Parse the input (e.g., "4 1 Hello everyone")
                String[] parts = input.split(" ", 3);
                if (parts.length < 3) {
                    System.out.println("Invalid format. Use: senderId chatId message");
                    continue;
                }

                String senderId = (parts[0]);
                Long chatId = Long.parseLong(parts[1]);
                String content = parts[2];

                // 3. Construct the MessageDTO
                MessageDTO dto = new MessageDTO();
                dto.setSenderId(senderId);
                dto.setChatId(chatId);
                dto.setContent(content);
                dto.setSentAt(Instant.now());
                dto.setMessageType("regular");

                // 4. Convert to JSON and Send
                String json = mapper.writeValueAsString(dto);
                webSocket.sendText(json, true).join();
                System.out.println("[SENT] " + json);

            } catch (NumberFormatException e) {
                System.out.println("Error: senderId and chatId must be numbers.");
            } catch (Exception e) {
                System.out.println("Error sending message: " + e.getMessage());
            }
        }

        // Cleanup
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Tester exiting").join();
        System.out.println("Disconnected.");
    }

    // Listener to handle incoming messages from the server
    private static class WebSocketListener implements WebSocket.Listener {
        @Override
        public void onOpen(WebSocket webSocket) {
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            System.out.println("\n[RECEIVED] " + data);
            System.out.print("> "); // Reprint the prompt so it looks clean
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            System.out.println("\n[ERROR] " + error.getMessage());
            WebSocket.Listener.super.onError(webSocket, error);
        }
    }
}