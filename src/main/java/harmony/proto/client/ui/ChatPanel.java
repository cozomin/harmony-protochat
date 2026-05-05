package harmony.proto.client.ui;

import harmony.proto.dto.ChatDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionListener;
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

    private final JTextArea messagesArea = new JTextArea();
    private final JLabel headerLabel = new JLabel("Not connected");

    public ChatPanel() {
        init();
    }

    private void init() {
        root.setLayout(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel bottomBar = new JPanel(new BorderLayout(2, 2));

        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 18f));

        messagesArea.setEditable(false);
        messagesArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        messagesArea.setLineWrap(true);
        messagesArea.setWrapStyleWord(true);
        messagesArea.setText("Chat backend connected.\n\nMessage loading is not wired yet.");

        bottomBar.add(txtMessage,  BorderLayout.CENTER);
        bottomBar.add(bSend,  BorderLayout.EAST);

        root.add(headerLabel, BorderLayout.NORTH);
        root.add(new JScrollPane(messagesArea), BorderLayout.CENTER);
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

    public void showSession(String username, List<ChatDTO> chats, boolean connected) {

        SwingUtilities.invokeLater(() -> {
            headerLabel.setText("Welcome " + username);
            messagesArea.setText(
                    "Connection status: " + (connected ? "connected" : "disconnected") + "\n" +
                            "Logged in as: " + username + "\n" +
                            "The UI is now connected to the websocket client backend."
            );
        });

    }

    public void showMessage(String message) {
        messagesArea.append(message);
    }

    public void prepareLoadChats() {
        messagesArea.setText("");
    }

    public void prepareLoadMessages() {
        messagesArea.setText("");
    }

    public void clearTxtMessages() {
        SwingUtilities.invokeLater(() -> {txtMessage.setText("");});
    }
}