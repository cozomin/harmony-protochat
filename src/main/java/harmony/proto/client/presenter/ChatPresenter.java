package harmony.proto.client.presenter;

import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.ChatPanel;
import harmony.proto.client.ui.MessagePanel;
import harmony.proto.dto.ChatDTO;
import harmony.proto.dto.MessageDTO;
import harmony.proto.dto.res.MessageUpdateAction;

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

        chatView.setPolishAction(e -> {
            String currentText = chatView.getTxtMessage();

            if (currentText == null || currentText.trim().length() < 3) return;

            chatView.setSendEnabled(false);
            chatView.setPolishEnabled(false);
            chatView.updateTxtMessage("AI is thinking...");

            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() throws Exception {
                    return client.polishMessageText(currentText);
                }

                @Override
                protected void done() {
                    try {
                        String polishedText = get();
                        chatView.updateTxtMessage(polishedText);
                    } catch (Exception ex) {
                        chatView.updateTxtMessage(currentText);
                        ex.printStackTrace();
                    } finally {
                        chatView.setSendEnabled(true);
                        chatView.setPolishEnabled(true);
                    }
                }
            };
            worker.execute();
        });

        client.setLiveMessageListener(message -> {
            SwingUtilities.invokeLater(() -> {
                // Check if the incoming message belongs to the currently open chat
                activeChat = inboxPresenter.getActiveChat();

                if (activeChat != null && activeChat.getChatID().equals(message.getChatId())) {
                    String time = message.getSentAt() == null ? "Now" : timeFormatter.format(message.getSentAt());
                    String shownMessage = "[" + time + "] "
                            + message.getSenderId() + ": "
                            + message.getContent()
                            + "\n";

                    boolean isMine = message.getSenderId().equals(client.getCurrentUsername());
                    chatView.showMessage(message, isMine);

                    attachMessageActions(message, isMine);
                }
                else {
                    // In app notification goes here
                }
            });
        });

        try {
            client.setLiveMessageUpdateListener(updateEvent -> {
//                System.out.println("Presenter processing update for ID: " + updateEvent.getMessId());
                SwingUtilities.invokeLater(() -> {
                    MessagePanel mp = chatView.findMessagePanelById(updateEvent.getMessId());
                    if (mp != null) {
//                        System.out.println("MessagePanel FOUND! Applying update.");
                        if (updateEvent.getAction() == MessageUpdateAction.EDIT) {
                            mp.updateText(updateEvent.getNewContent());
                        } else if (updateEvent.getAction() == MessageUpdateAction.DELETE) {
                            chatView.removeMessage(mp);
                        }
                    } else {
                        System.out.println("MessagePanel NOT FOUND for ID: " + updateEvent.getMessId());
                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                        boolean isMine = message.getSenderId().equals(client.getCurrentUsername());
                        chatView.showMessage(message, isMine);

                        attachMessageActions(message, isMine);
                    }

                    if (messages.isEmpty()){
                        MessageDTO message = new MessageDTO();
                        message.setContent("No messages found");
                        chatView.showMessage(message, false);
                    }
                } catch(Exception e){
//                    chatView.showMessage(e.getMessage());
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
//            chatView.showMessage(e.getMessage());
        }
    }

    private void attachMessageActions(MessageDTO message, boolean isMine) {
        if (!isMine) return;

        SwingUtilities.invokeLater(() -> {
            MessagePanel mp = chatView.findMessagePanelById(message.getMessId());
            if (mp != null) {
                mp.setEditAction(e -> {
                    String newContent = JOptionPane.showInputDialog(chatView, "Edit message:", message.getContent());
                    if (newContent != null && !newContent.trim().isEmpty() && !newContent.equals(message.getContent())) {
                        try {
                            client.editMessage(message.getMessId(), message.getChatId(), newContent);
                            message.setContent(newContent);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                mp.setDeleteAction(e -> {
                    int confirm = JOptionPane.showConfirmDialog(chatView,
                            "Are you sure you want to delete this message?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            client.deleteMessage(message.getMessId(), message.getChatId());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}