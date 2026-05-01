package harmony.proto.client.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class ChatPanel extends JPanel {

    private final JTextField userIdField = new JTextField("", 10);
    private final JButton loadButton = new JButton("Refresh");
    private final JButton logoutButton = new JButton("Logout");

    private final JTextArea messagesArea = new JTextArea();
    private final JLabel headerLabel = new JLabel("Not connected");

    public ChatPanel() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.add(new JLabel("User ID:"));
        userIdField.setEditable(false);
        topBar.add(userIdField);
        topBar.add(loadButton);
        topBar.add(logoutButton);

        JPanel content = new JPanel(new BorderLayout(8, 8));
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 18f));

        messagesArea.setEditable(false);
        messagesArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        messagesArea.setLineWrap(true);
        messagesArea.setWrapStyleWord(true);
        messagesArea.setText("Chat backend connected.\n\nMessage loading is not wired yet.");

        content.add(headerLabel, BorderLayout.NORTH);
        content.add(new JScrollPane(messagesArea), BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    public void setLogoutAction(ActionListener actionListener) {
        logoutButton.addActionListener(actionListener);
    }

    public void setLoadAction(ActionListener actionListener) {
        loadButton.addActionListener(actionListener);
    }

    public void showSession(long userId, String username, boolean connected) {
        SwingUtilities.invokeLater(() -> {
            userIdField.setText(String.valueOf(userId));
            headerLabel.setText("Welcome " + username);
            messagesArea.setText(
                    "Connection status: " + (connected ? "connected" : "disconnected") + "\n" +
                            "Logged in as: " + username + "\n" +
                            "User ID: " + userId + "\n\n" +
                            "The UI is now connected to the websocket client backend."
            );
        });
    }
}