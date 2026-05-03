package harmony.proto.client.presenter;

import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.ChatPanel;
import harmony.proto.dto.ChatDTO;
import harmony.proto.dto.MessageDTO;

import javax.swing.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatPresenter {
    private final ChatPanel chatView;
    private final WebSocketClient client;
    private final ClientPresenter coordinator;

    public ChatPresenter(ChatPanel ChatView, WebSocketClient client, ClientPresenter coordinator) {
        this.chatView = ChatView;
        this.client = client;
        this.coordinator = coordinator;
        bindChatActions();
    }

    private void bindChatActions() {

        chatView.setSendMessageAction(e -> {
            ChatDTO selectedGroup = chatView.getGroupsList().getSelectedValue();
            ChatDTO selectedDm = chatView.getDmsList().getSelectedValue();
            ChatDTO activeChat = selectedGroup != null ? selectedGroup : selectedDm;

            sendMessage(activeChat.getChatID());
        });
        chatView.setLogoutAction(e -> logout());
        chatView.setLoadAction(e -> loadSessionInfoIntoChat());

        chatView.setGroupListAction(e -> {
            if (!e.getValueIsAdjusting()) {
                ChatDTO selected = chatView.getGroupsList().getSelectedValue();
                if (selected != null) {
                    chatView.getDmsList().clearSelection();
                    loadMessages(selected);
                }
            }
        });

        chatView.setDmsListAction(e -> {
            if (!e.getValueIsAdjusting()) {
                ChatDTO selected = chatView.getDmsList().getSelectedValue();
                if (selected != null) {
                    chatView.getGroupsList().clearSelection();
                    loadMessages(selected);
                }
            }
        });

        client.setLiveMessageListener(message -> {
            SwingUtilities.invokeLater(() -> {
                // Check if the incoming message belongs to the currently open chat

                ChatDTO selectedGroup = chatView.getGroupsList().getSelectedValue();
                ChatDTO selectedDm = chatView.getDmsList().getSelectedValue();
                ChatDTO activeChat = selectedGroup != null ? selectedGroup : selectedDm;

                if (activeChat != null && activeChat.getChatID().equals(message.getChatId())) {

                    String time = message.getSentAt() == null ? "Now" : timeFormatter.format(message.getSentAt());
                    String shownMessage = "[" + time + "] "
                            + message.getSenderId() + ": "
                            + message.getContent()
                            + "\n";

                    chatView.showMessage(shownMessage);
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
        String username = client.getCurrentUsername();
        List<ChatDTO> chats = client.getChats();
        chatView.showSession(
                username != null ? username : "Unknown user",
                chats,
                client.isConnected()
        );
    }

    public void loadChats() throws Exception{
        chatView.prepareLoadChats();

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
                                chatView.getGroupsListModel().addElement(chat);
                            } else {
                                chatView.getDmsListModel().addElement(chat);
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
                    chatView.prepareLoadMessages();
                    for(MessageDTO message : messages){
                        String time = message.getSentAt() == null ? "?" : timeFormatter.format(message.getSentAt());
                        String shownMessage = "[" + time + "] "
                                + message.getSenderId() + ": "
                                + message.getContent()
                                + "\n";
                        chatView.showMessage(shownMessage);
                    }

                    if (messages.isEmpty()){
                        chatView.showMessage("No messages");
                    }
                } catch(Exception e){
                    chatView.showMessage(e.getMessage());
                }
            }
        };
        worker.execute();
    }

    public void sendMessage(Long chatID) {
        chatView.setSendEnabled(false);
        String message = chatView.getTxtMessage();
        chatView.clearTxtMessages();

        try {
            client.sendMessage(message, chatID);
            chatView.setSendEnabled(true);
        }
        catch (Exception e) {

        }
    }
}
