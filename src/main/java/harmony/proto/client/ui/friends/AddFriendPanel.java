package harmony.proto.client.ui.friends;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class AddFriendPanel extends JPanel {
    private final JTextField txtUsername = new JTextField(20);
    private final JButton sendRequestButton = new JButton("Send Request");
    private final JLabel statusLabel = new JLabel(" "); // To show success/error messages

    public AddFriendPanel() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        JLabel headerLabel = new JLabel("Add a Friend");
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 18f));
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel instructionLabel = new JLabel("Enter their username below to send a request.");
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputPanel.add(txtUsername);
        inputPanel.add(sendRequestButton);

        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(Color.GRAY);

        formPanel.add(headerLabel);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(instructionLabel);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(inputPanel);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(statusLabel);

        add(formPanel, BorderLayout.NORTH);
    }

    public String getUsernameInput() { return txtUsername.getText().trim(); }
    public void clearInput() { txtUsername.setText(""); }
    public void setStatusMessage(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }
    public void setSendRequestAction(ActionListener actionListener) {
        sendRequestButton.addActionListener(actionListener);
        txtUsername.addActionListener(actionListener);
    }
    public JButton getSendRequestButton() { return sendRequestButton; }
}