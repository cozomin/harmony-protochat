package harmony.proto.client.presenter;

import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.ChatPanel;
import harmony.proto.dto.ChatDTO;
import harmony.proto.dto.MessageDTO;

import javax.swing.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ChatPresenter {
    private final ChatPanel ChatView;
    private final WebSocketClient client;
    private final ClientPresenter coordinator;

    public ChatPresenter(ChatPanel ChatView, WebSocketClient client, ClientPresenter coordinator) {
        this.ChatView = ChatView;
        this.client = client;
        this.coordinator = coordinator;
        bindChatActions();
    }

    private void bindChatActions() {
        ChatView.setLogoutAction(e -> logout());
        ChatView.setLoadAction(e -> loadSessionInfoIntoChat());

        ChatView.setGroupListAction(e -> {
            if (!e.getValueIsAdjusting()) {
                ChatDTO selected = ChatView.getGroupsList().getSelectedValue();
                if (selected != null) {
                    ChatView.getDmsList().clearSelection();
                    loadMessages(selected);
                }
            }
        });

        ChatView.setDmsListAction(e -> {
            if (!e.getValueIsAdjusting()) {
                ChatDTO selected = ChatView.getDmsList().getSelectedValue();
                if (selected != null) {
                    ChatView.getGroupsList().clearSelection();
                    loadMessages(selected);
                }
            }
        });

        client.setLiveMessageListener(message -> {
            SwingUtilities.invokeLater(() -> {
                // Check if the incoming message belongs to the currently open chat
                ChatDTO selectedGroup = ChatView.getGroupsList().getSelectedValue();
                ChatDTO selectedDm = ChatView.getDmsList().getSelectedValue();
                ChatDTO activeChat = selectedGroup != null ? selectedGroup : selectedDm;

                if (activeChat != null && activeChat.getChatID().equals(message.getChatId())) {

                    String time = message.getSentAt() == null ? "Now" : timeFormatter.format(message.getSentAt());
                    String shownMessage = "[" + time + "] "
                            + "user " + message.getSenderId() + ": "
                            + message.getContent()
                            + "\n";

                    ChatView.showMessage(shownMessage);
                }
            });
        });
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
        List<ChatDTO> chats = client.getChats();
        ChatView.showSession(
                userID != null ? userID : -1L,
                username != null ? username : "Unknown user",
                chats,
                client.isConnected()
        );
    }

    public void loadChats() throws Exception{
        client.fetchChats();
        List<ChatDTO> chats = client.getChats();
        ChatView.prepareLoadChats();

        SwingWorker<List<ChatDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ChatDTO> doInBackground() throws Exception {
                return chats;
            }
            @Override
            protected void done() {
                try{
                    List<ChatDTO> chats = get();
                    for(ChatDTO chat : chats){
                        if(chat.isGroup()) {
                            ChatView.getGroupsListModel().addElement(chat);
                        } else {
                            ChatView.getDmsListModel().addElement(chat);
                        }
                    }
                } catch (Exception e){

                }
            }
        };

        worker.execute();
    }

    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.systemDefault());

    public void loadMessages(ChatDTO chat) {

        SwingWorker<List<MessageDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<MessageDTO> doInBackground() throws Exception {
                 return client.fetchMessages(chat.getChatID());
            }

            @Override
            protected void done() {
                try {
                    List<MessageDTO> messages = get();
                    ChatView.prepareLoadMessages();
                    for(MessageDTO message : messages){
                        String time = message.getSentAt() == null ? "?" : timeFormatter.format(message.getSentAt());
                        String shownMessage = "[" + time + "] "
                                + "user " + message.getSenderId() + ": "
                                + message.getContent()
                                + "\n";
                        ChatView.showMessage(shownMessage);
                    }

                    if (messages.isEmpty()){
                        ChatView.showMessage("No messages");
                    }
                } catch(Exception e){
                    ChatView.showMessage(e.getMessage());
                }
            }
        };
        worker.execute();
    }
}
