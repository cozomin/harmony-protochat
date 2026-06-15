package harmony.proto.client.presenter;

import harmony.proto.client.PaneSelector;
import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.InboxPanel;
import harmony.proto.dto.ChatDTO;
import harmony.proto.dto.res.LeaveGroupRes;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
            inboxView.hideGroupMembersPanel();
            inboxView.showPane(PaneSelector.FRIENDS);
        });

        inboxView.setLogoutAction(e -> logout());
        inboxView.setLoadAction(e -> {
            chatPresenter.loadSessionInfo();
            loadChats();
            friendsPresenter.refreshAllLists();
        });

        inboxView.setGroupListAction(e -> {
            if (!e.getValueIsAdjusting()) {
                ChatDTO selected = inboxView.getGroupsList().getSelectedValue();
                if (selected != null) {
                    inboxView.getDmsList().clearSelection();
                    inboxView.showPane(PaneSelector.CHATBOX);
                    chatPresenter.loadMessages(selected);
                }

                if (inboxView.getMembersList().isShowing()) {
                    loadChatMembers();
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
                    inboxView.hideGroupMembersPanel();
                }
            }
        });

        inboxView.setMemberListAction( e -> {
            inboxView.getMembersList().clearSelection();
            inboxView.showGroupMembersPanel();
            loadChatMembers();
        });

        inboxView.setLeaveButtonAction( e -> {
            leaveGroup();
        });

        inboxView.setInterestsButtonAction( e -> {
            try {
                coordinator.onInterestShow();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        client.setLiveGroupCreationListener( groupCreation -> {
            loadChats();
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
                        inboxView.prepareLoadChats();

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

    public void loadChatMembers() {
        ChatDTO selectedGroup = inboxView.getGroupsList().getSelectedValue();
        if (selectedGroup == null) {
            return;
        }

        SwingWorker<harmony.proto.dto.res.ChatMembersRes, Void> worker = new SwingWorker<>() {
            @Override
            protected harmony.proto.dto.res.ChatMembersRes doInBackground() throws Exception {
                // Fetch the object containing both members and topics
                return client.fetchChatMembers(selectedGroup.getChatID());
            }

            @Override
            protected void done() {
                try {
                    inboxView.prepareLoadChatMembers(); // Clears member list
                    inboxView.prepareLoadTopics();      // Clears topic list

                    harmony.proto.dto.res.ChatMembersRes res = get();

                    if (res != null && res.isSuccess()) {
                        // Populate Members
                        if (res.getChatMembers() != null) {
                            for (String member : res.getChatMembers()) {
                                inboxView.getMembersListModel().addElement(member);
                            }
                        }

                        // Populate Topics
                        if (res.getChatTopics() != null && !res.getChatTopics().isEmpty()) {
                            for (String topic : res.getChatTopics()) {
                                inboxView.getTopicsListModel().addElement("$ " + topic);
                            }
                        } else {
                            inboxView.getTopicsListModel().addElement("No topics set");
                        }
                    } else {
                        System.out.println("Failed to load group details");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    public void leaveGroup(){
        ChatDTO selectedGroup = inboxView.getGroupsList().getSelectedValue();
        if (selectedGroup == null) {
            return;
        }

        SwingWorker<LeaveGroupRes, Void> worker = new SwingWorker<>() {
            @Override
            protected LeaveGroupRes doInBackground() throws Exception {
                return client.leaveGroup(selectedGroup.getChatName());
            }

            @Override
            protected void done() {
                try {

                    LeaveGroupRes res = get();

                    if (res != null && res.isSuccess()) {
                        loadChats();
                    } else {
                        System.out.println("Failed to leave group");
                    }
                } catch (Exception e) {
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
