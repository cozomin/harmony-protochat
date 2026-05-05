package harmony.proto.client.presenter;

import harmony.proto.client.PaneSelector;
import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.InboxPanel;
import harmony.proto.dto.ChatDTO;

import javax.swing.*;
import java.util.List;

public class InboxPresenter {

    private final InboxPanel inboxView;
    private final WebSocketClient client;
    private final ClientPresenter coordinator;
    private final ChatPresenter chatPresenter;
    private final FriendsPresenter friendsPresenter;

    public InboxPresenter(InboxPanel inboxView, WebSocketClient client, ClientPresenter coordinator) {
        this.inboxView = inboxView;
        this.client = client;
        this.coordinator = coordinator;

        this.chatPresenter = new ChatPresenter(inboxView.getChatView(), client, this);
        this.friendsPresenter = new FriendsPresenter(inboxView.getFriendsPanel(), client, this);

        bindInboxActions();
        inboxView.showPane(PaneSelector.FRIENDS);
    }

    private void bindInboxActions() {
        inboxView.setFriendsButtonAction(e -> {
            inboxView.getDmsList().clearSelection();
            inboxView.getGroupsList().clearSelection();
            inboxView.showPane(PaneSelector.FRIENDS);
        });

        inboxView.setLogoutAction(e -> logout());
        inboxView.setLoadAction(e -> {
            chatPresenter.loadSessionInfo();
            loadChats();
        });

        inboxView.setGroupListAction(e -> {
            if (!e.getValueIsAdjusting()) {
                ChatDTO selected = inboxView.getGroupsList().getSelectedValue();
                if (selected != null) {
                    inboxView.getDmsList().clearSelection();
                    inboxView.showPane(PaneSelector.CHATBOX);
                    chatPresenter.loadMessages(selected);
                }
            }
        });

        inboxView.setDmsListAction(e -> {
            if (!e.getValueIsAdjusting()) {
                ChatDTO selected = inboxView.getDmsList().getSelectedValue();
                if (selected != null) {
                    inboxView.getGroupsList().clearSelection();
                    inboxView.showPane(PaneSelector.CHATBOX);
                    chatPresenter.loadMessages(selected);
                }
            }
        });
    }

    private void logout() {
        try{
            client.disconnect();
        }
        catch (Exception e){}

        coordinator.onLogout();
    }

    public void openDirectMessage(String targetUsername) {
        DefaultListModel<ChatDTO> dmsModel = inboxView.getDmsListModel();

        // Search through the loaded DMs for the matching username
        for (int i = 0; i < dmsModel.getSize(); i++) {
            ChatDTO chat = dmsModel.getElementAt(i);

            if (!chat.isGroup() && chat.getChatName().equals(targetUsername)) {
                inboxView.getDmsList().setSelectedIndex(i);
                inboxView.getGroupsList().clearSelection();

                inboxView.showPane(PaneSelector.CHATBOX);
                chatPresenter.loadMessages(chat);
                return;
            }
        }

        loadChats();
    }

    public void loadChats(){
        inboxView.prepareLoadChats();

        SwingWorker<List<ChatDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ChatDTO> doInBackground() throws Exception {
                client.fetchChats();
                return client.getChats();
            }
            @Override
            protected void done() {
                try{
                    List<ChatDTO> chats = get();
                    if (chats != null) {
                        for (ChatDTO chat : chats) {
                            if (chat.isGroup()) {
                                inboxView.getGroupsListModel().addElement(chat);
                            } else {
                                inboxView.getDmsListModel().addElement(chat);
                            }
                        }
                    }
                    else{
                        System.out.println("No chats found");
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    public InboxPanel getInboxView() {
        return inboxView;
    }

    ChatDTO getActiveChat(){
        return inboxView.getDmsList().getSelectedValue() != null ? inboxView.getDmsList().getSelectedValue() : inboxView.getGroupsList().getSelectedValue() ;
    }

    public FriendsPresenter getFriendsPresenter() {
        return friendsPresenter;
    }

    public void loadSessionInfo() {
         chatPresenter.loadSessionInfo();
    }
}
