package harmony.proto.client.presenter;

import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.ChatPanel;
import harmony.proto.client.ui.LoginPanel;

public class ChatPresenter {
    private final ChatPanel view;
    private final WebSocketClient client;
    private final ClientPresenter coordinator;

    public ChatPresenter(ChatPanel view, WebSocketClient client, ClientPresenter coordinator) {
        this.view = view;
        this.client = client;
        this.coordinator = coordinator;
        bindChatActions();
    }

    private void bindChatActions() {
        view.setLogoutAction(e -> logout());
        view.setLoadAction(e -> loadSessionInfoIntoChat());
    }

    private void logout() {
        try{
            client.disconnect();
        }
        catch (Exception e){}

        coordinator.onLogout();
    }

    public void loadSessionInfoIntoChat() {
        Long userID = client.getCurrentUserId();
        String username = client.getCurrentUsername();
        view.showSession(
                userID != null ? userID : -1L,
                username != null ? username : "Unknown user",
                client.isConnected()
        );
    }
}
