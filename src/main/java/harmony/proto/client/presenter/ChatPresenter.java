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
    private final InboxPresenter inboxPresenter;
    private ChatDTO activeChat;

    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                    .withZone(ZoneId.systemDefault());

    public ChatPresenter(ChatPanel ChatView, WebSocketClient client, InboxPresenter inboxPresenter) {
        this.chatView = ChatView;
        this.client = client;
        this.inboxPresenter = inboxPresenter;
        bindChatActions();
    }

    private void bindChatActions() {

        chatView.setSendMessageAction(e -> sendMessage());

        client.setLiveMessageListener(message -> {
            SwingUtilities.invokeLater(() -> {
                // Check if the incoming message belongs to the currently open chat
                activeChat = inboxPresenter.getActiveChat();
//                System.out.println(activeChat.getChatName());

                if (activeChat != null && activeChat.getChatID().equals(message.getChatId())) {
                    String time = message.getSentAt() == null ? "Now" : timeFormatter.format(message.getSentAt());
                    String shownMessage = "[" + time + "] "
                            + message.getSenderId() + ": "
                            + message.getContent()
                            + "\n";

                    chatView.showMessage(shownMessage);
//                    System.out.println(message.getContent());
                }
                else {
                    // In app notification goes here
                }
            });
        });
    }

    public void loadSessionInfo() {
        String username = client.getCurrentUsername();
        List<ChatDTO> chats = client.getChats();
        chatView.showSession(
                username != null ? username : "Unknown user",
                chats,
                client.isConnected()
        );
    }

    public void loadMessages(ChatDTO chat) {
        this.activeChat = chat;

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

    public void sendMessage() {
        if (activeChat == null) return;

        chatView.setSendEnabled(false);
        String message = chatView.getTxtMessage();

        if (message.isEmpty()){
            chatView.setSendEnabled(true);
            return;
        }

        chatView.clearTxtMessages();

        try {
            client.sendMessage(message, activeChat.getChatID());
            chatView.setSendEnabled(true);
        }
        catch (Exception e) {
            chatView.setSendEnabled(true);
            chatView.showMessage(e.getMessage());
        }
    }
}
