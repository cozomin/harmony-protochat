package harmony.proto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Scanner;
import java.util.concurrent.CompletionStage;

public class WebSocketClientJavaNET {

    public static void main(String[] args) {
        System.out.println("Connecting to chat server...");

        HttpClient client = HttpClient.newHttpClient();

        // Connect to the Netty WebSocket endpoint we defined ("/chat")
        WebSocket webSocket = client.newWebSocketBuilder()
//                .buildAsync(URI.create("ws://localhost:8080/chat"), new WebSocketClientListener())
                .buildAsync(URI.create("wss://harmony-chat.space/chat"), new WebSocketClientListener())
                .join();

        System.out.println("Connected! Type your messages below:");

        // Listen to terminal input and send to the server
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if ("exit".equalsIgnoreCase(line)) {
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "User exited").join();
                break;
            }
            // Send the terminal input as a WebSocket text frame
            webSocket.sendText(line, true).join();
        }

        scanner.close();
    }

    // Listener for incoming messages from the Netty server
    private static class WebSocketClientListener implements WebSocket.Listener {
        @Override
        public void onOpen(WebSocket webSocket) {
            // Signal that we are ready to receive the first message
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            // Print the incoming chat message to the terminal
            System.out.println(data);

            // Request the next message from the server
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            System.out.println("Connection closed by server: " + reason);
            System.exit(0);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            System.err.println("Error occurred: " + error.getMessage());
        }
    }
}