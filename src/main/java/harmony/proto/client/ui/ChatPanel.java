package harmony.proto.client.ui;

import harmony.proto.dto.ChatDTO;
import harmony.proto.dto.MessageDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

public class ChatPanel extends JPanel {

    private final JPanel root =  new JPanel(new BorderLayout());
    private final JTextField txtMessage = new JTextField();
    private final JButton bSend = new JButton("Send")
    {
        @Override
        public boolean isDefaultButton() {
            return true;
        }
    };

    URL imgUrl = getClass().getResource("/img/icons/ai.png");
    JButton bPolish;

    private final JPanel messagesContainer = new JPanel();
    private JScrollPane scrollPane;

    private final JLabel headerLabel = new JLabel("Not connected");

    public ChatPanel() {
        init();
    }

    private void init() {
        if (imgUrl != null) {
            ImageIcon originalIcon = new ImageIcon(imgUrl);

            Image scaledImage = originalIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);

            bPolish = new JButton(new ImageIcon(scaledImage));
        }
        else {
            bPolish = new JButton("\u2728");
        }

        root.setLayout(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel bottomBar = new JPanel(new BorderLayout(2, 2));

        messagesContainer.setLayout(new BoxLayout(messagesContainer, BoxLayout.Y_AXIS));

        JPanel scrollWrapper = new JPanel(new BorderLayout());
        scrollWrapper.add(messagesContainer, BorderLayout.NORTH);

        scrollPane = new JScrollPane(scrollWrapper);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 18f));

        bottomBar.add(txtMessage, BorderLayout.CENTER);
        JPanel actionButtons = new JPanel(new GridLayout(1, 2, 5, 0));
        actionButtons.add(bPolish);
        actionButtons.add(bSend);
        bottomBar.add(actionButtons, BorderLayout.EAST);

        bPolish.setToolTipText("Rewrite with AI");

        root.add(headerLabel, BorderLayout.NORTH);
        root.add(scrollPane, BorderLayout.CENTER);
        root.add(bottomBar, BorderLayout.SOUTH);

        this.setLayout(new BorderLayout());
        this.add(root, BorderLayout.CENTER);
    }

    public String getTxtMessage() {
        return txtMessage.getText().trim();
    }
    public void setMessage(String message) {}

    public void setSendMessageAction(ActionListener actionListener) {
        txtMessage.addActionListener(actionListener);
        bSend.addActionListener(actionListener);
    }

    public void setSendEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> bSend.setEnabled(enabled));
    }

    public void setPolishAction(ActionListener actionListener) {
        bPolish.addActionListener(actionListener);
    }

    public void setPolishEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> bPolish.setEnabled(enabled));
    }

    public void updateTxtMessage(String text) {
        SwingUtilities.invokeLater(() -> txtMessage.setText(text));
    }

    public void showSession(String username, List<ChatDTO> chats, boolean connected) {

        SwingUtilities.invokeLater(() -> {
            headerLabel.setText("Welcome " + username);
//            messagesArea.setText(
//                    "Connection status: " + (connected ? "connected" : "disconnected") + "\n" +
//                            "Logged in as: " + username + "\n" +
//                            "The UI is now connected to the websocket client backend."
//            );
        });

    }

    public void showMessage(MessageDTO message, boolean isOwnMessage) {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            boolean isAtBottom = (vertical.getValue() + vertical.getVisibleAmount() + 10) >= vertical.getMaximum();

            MessagePanel bubble = new MessagePanel(message, isOwnMessage);

            JPanel alignWrapper = new JPanel(new FlowLayout(isOwnMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
            alignWrapper.setOpaque(false);
            alignWrapper.add(bubble);

            messagesContainer.add(alignWrapper);
            messagesContainer.add(Box.createVerticalStrut(6));

            messagesContainer.revalidate();
            messagesContainer.repaint();

            if (isAtBottom || isOwnMessage) {
                SwingUtilities.invokeLater(() -> {
                    vertical.setValue(vertical.getMaximum());
                });
            }
        });
    }

    public void removeMessage(MessagePanel messagePanel) {
        SwingUtilities.invokeLater(() -> {
            Container wrapper = messagePanel.getParent();
            if (wrapper != null) {
                messagesContainer.remove(wrapper);
                messagesContainer.revalidate();
                messagesContainer.repaint();
            }
        });
    }

    public MessagePanel findMessagePanelById(Long messId) {
        if (messId == null) {
            return null;
        }
        for (Component comp : messagesContainer.getComponents()) {
            if (comp instanceof JPanel alignWrapper) {
                for (Component inner : alignWrapper.getComponents()) {
                    if (inner instanceof MessagePanel mp) {
                        Long panelMsgId = mp.getMessageId();
                        if (panelMsgId != null && panelMsgId.equals(messId)) {
                            return mp;
                        }
                    }
                }
            }
        }
        return null;
    }

//    public void scrollToBottom() {
//        SwingUtilities.invokeLater(() -> {
//            messagesContainer.
//        })
//    }

    public void prepareLoadChats() {
        prepareLoadMessages();
    }

    public void prepareLoadMessages() {
        SwingUtilities.invokeLater(() -> {
            messagesContainer.removeAll();
            messagesContainer.revalidate();
            messagesContainer.repaint();
        });
    }

    public void clearTxtMessages() {
        SwingUtilities.invokeLater(() -> {txtMessage.setText("");});
    }
}