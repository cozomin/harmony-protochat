package harmony.proto.client.ui;

import harmony.proto.dto.ChatDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class ChatPanel extends JPanel {

    private final JTextField userIdField = new JTextField("", 10);
    private final JButton loadButton = new JButton("Refresh");
    private final JButton logoutButton = new JButton("Logout");

    private final DefaultListModel<ChatDTO> groupsListModel = new DefaultListModel<>();
    private final DefaultListModel<ChatDTO> dmsListModel = new DefaultListModel<>();
    private final JList<ChatDTO> groupsList = new JList<>(groupsListModel);
    private final JList<ChatDTO> dmsList = new JList<>(dmsListModel);

    private final JTextArea messagesArea = new JTextArea();
    private final JLabel headerLabel = new JLabel("Not connected");

    public ChatPanel() {
        init();
    }

    private void init() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.add(new JLabel("User ID:"));
        userIdField.setEditable(false);
        topBar.add(userIdField);
        topBar.add(loadButton);
        topBar.add(logoutButton);

        JPanel sideBar = new JPanel();
        sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS));
        sideBar.setPreferredSize(new Dimension(320, 0));

        JLabel groupsLabel = new JLabel("Groups");
        groupsLabel.setBorder(new EmptyBorder(0, 0, 6, 0));

        JLabel dmsLabel = new JLabel("Direct Messages");
        dmsLabel.setBorder(new EmptyBorder(12, 0, 6, 0));

        groupsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dmsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        groupsList.setCellRenderer(new ChatRenderer());
        dmsList.setCellRenderer(new ChatRenderer());

        sideBar.add(groupsLabel);
        sideBar.add(new JScrollPane(groupsList));
        sideBar.add(dmsLabel);
        sideBar.add(new JScrollPane(dmsList));

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
        add(sideBar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
    }

    public JList<ChatDTO> getDmsList() {
        return dmsList;
    }

    public JList<ChatDTO> getGroupsList() {
        return groupsList;
    }

    public DefaultListModel<ChatDTO> getGroupsListModel() {
        return groupsListModel;
    }

    public DefaultListModel<ChatDTO> getDmsListModel() {
        return dmsListModel;
    }

    public void setLogoutAction(ActionListener actionListener) {
        logoutButton.addActionListener(actionListener);
    }

    public void setGroupListAction(ListSelectionListener listSelectionListener) {
        groupsList.addListSelectionListener(listSelectionListener);
    }

    public void setDmsListAction(ListSelectionListener listSelectionListener) {
        dmsList.addListSelectionListener(listSelectionListener);
    }

    public void setLoadAction(ActionListener actionListener) {
        loadButton.addActionListener(actionListener);
    }

    public void showSession(long userId, String username, List<ChatDTO> chats, boolean connected) {

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

    public void showMessage(String message) {
       messagesArea.append(message);
    }

    public void prepareLoadChats() {
        groupsListModel.clear();
        dmsListModel.clear();
        messagesArea.setText("");
    }

    public void prepareLoadMessages() {
        messagesArea.setText("");
    }
}

class ChatRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus
    ) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof ChatDTO chat) {
            String prefix = chat.isGroup() ? "# " : "@ ";
            setText(prefix + chat.getChatName() + "  (" + chat.getChatID() + ")");
        }
        return this;
    }
}
