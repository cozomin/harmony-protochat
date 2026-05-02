package harmony.proto.client.presenter;

import harmony.proto.client.PaneSelector;
import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.ClientUI;

import javax.swing.*;

// This presenter acts as a coordinator for the specific ones

public class ClientPresenter {
    private final WebSocketClient client;
    private final ClientUI mainView;

    private final LoginPresenter loginPresenter;
    private final ChatPresenter chatPresenter;

    public ClientPresenter(WebSocketClient client, ClientUI view) {
        this.client = client;
        this.mainView = view;

        this.loginPresenter = new LoginPresenter(mainView.getLoginView(), client, this);
        this.chatPresenter = new ChatPresenter(mainView.getChatView(), client, this);

        mainView.showPane(PaneSelector.LOGIN);
    }

    public void onLoginSuccess() throws Exception{
        chatPresenter.loadSessionInfoIntoChat();
        mainView.showPane(PaneSelector.CHAT);
        chatPresenter.loadChats();

    }

    public void onLogout() {
        mainView.getLoginView().clearPassword();
        mainView.showPane(PaneSelector.LOGIN);
    }

    public void navigateTo(PaneSelector pane) {
        mainView.showPane(pane);
    }
}