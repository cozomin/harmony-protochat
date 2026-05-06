package harmony.proto.client.presenter;

import harmony.proto.client.PaneSelector;
import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.ClientUI;
import harmony.proto.client.ui.friends.FriendsPaneSelector;
import harmony.proto.dto.req.FriendOperation;

// This presenter acts as a coordinator for the specific ones

public class ClientPresenter {
    private final WebSocketClient client;
    private final ClientUI mainView;

    private final LoginPresenter loginPresenter;
    private final RegisterPresenter registerPresenter;
    private final InboxPresenter inboxPresenter;
//    private final ChatPresenter chatPresenter;

    public ClientPresenter(WebSocketClient client, ClientUI view) {
        this.client = client;
        this.mainView = view;

        this.loginPresenter = new LoginPresenter(mainView.getLoginView(), client, this);
        this.registerPresenter = new RegisterPresenter(mainView.getRegisterView(), client , this);
//        this.chatPresenter = new ChatPresenter(mainView.getInboxView().getChatView(), client, inboxPresenter);
        this.inboxPresenter = new InboxPresenter(mainView.getInboxView(), client, this);

        mainView.showPane(PaneSelector.LOGIN);
    }

    public void onLoginSuccess() throws Exception{
        inboxPresenter.loadSessionInfo();
        mainView.showPane(PaneSelector.INBOX);
        inboxPresenter.loadChats();
        inboxPresenter.getFriendsPresenter().refreshAllLists();
        inboxPresenter.getInboxView().showPane(PaneSelector.FRIENDS);
        inboxPresenter.getFriendsPresenter().refreshAllLists();
    }

    public void onLogout() {
        mainView.getLoginView().clearPassword();
        mainView.getRegisterView().clearPassword();
        mainView.getInboxView().getFriendsPanel().getAddFriendPanel().clearStatusMessage();
        mainView.getInboxView().getFriendsPanel().getAddFriendPanel().clearInput();
        mainView.getInboxView().getFriendsPanel().prepareLoadFriends(FriendOperation.fetch_accepted);
        mainView.getInboxView().getFriendsPanel().prepareLoadFriends(FriendOperation.fetch_outgoing);
        mainView.showPane(PaneSelector.LOGIN);
    }

    public void navigateTo(PaneSelector pane) {
        mainView.showPane(pane);
    }
}