package harmony.proto.client.ui;

import harmony.proto.client.PaneSelector;
import harmony.proto.client.ui.friends.FriendsPanel;
import harmony.proto.dto.ChatDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionListener;

public class InboxPanel extends JPanel {
    private final ChatPanel chatPanel = new ChatPanel();
    private final FriendsPanel friendsPanel = new FriendsPanel();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

//    private final JPanel root = new JPanel(new BorderLayout(12, 12));

    private final JButton loadButton = new JButton("Refresh");
    private final JButton logoutButton = new JButton("Logout");
    private final JButton friendsButton = new JButton("Friends");

    private final DefaultListModel<ChatDTO> groupsListModel = new DefaultListModel<>();
    private final DefaultListModel<ChatDTO> dmsListModel = new DefaultListModel<>();
    private final JList<ChatDTO> groupsList = new JList<>(groupsListModel);
    private final JList<ChatDTO> dmsList = new JList<>(dmsListModel);

    public InboxPanel() {
        init();
    }

    private void init() {
        // Layout initialization for the Inbox view
        this.setLayout(new BorderLayout(12, 12));
        this.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Content - Center
        contentPanel.add(chatPanel, PaneSelector.CHATBOX.name());
        contentPanel.add(friendsPanel, PaneSelector.FRIENDS.name());

        // Top bar - North
        JPanel topBar = new JPanel();
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.X_AXIS));
//        topBar.add(new JLabel("User ID:"));
//        userIdField.setEditable(false);
//        topBar.add(userIdField);
        topBar.add(friendsButton);
        topBar.add(Box.createHorizontalGlue());
        topBar.add(loadButton);
        topBar.add(logoutButton);

        // Message chats - West
        JPanel sideBar = new JPanel();
        sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS));
        sideBar.setPreferredSize(new Dimension(320, 0));

        JLabel groupsLabel = new JLabel("Groups");
        groupsLabel.setBorder(new EmptyBorder(0, 0, 6, 0));

        JLabel dmsLabel = new JLabel("Direct Messages");
        dmsLabel.setBorder(new EmptyBorder(12, 0, 6, 0));

        groupsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dmsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        groupsList.setCellRenderer(new InboxRenderer());
        dmsList.setCellRenderer(new InboxRenderer());

        sideBar.add(groupsLabel);
        sideBar.add(new JScrollPane(groupsList));
        sideBar.add(dmsLabel);
        sideBar.add(new JScrollPane(dmsList));

        add(topBar, BorderLayout.NORTH);
        add(sideBar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        cardLayout.show(contentPanel, PaneSelector.FRIENDS.name());
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

    public ChatPanel getChatView() {
        return chatPanel;
    }

    public FriendsPanel getFriendsPanel() {
        return friendsPanel;
    }

    public void setFriendsButtonAction(ActionListener actionListener) {
        friendsButton.addActionListener(actionListener);
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

    public void prepareLoadChats() {
        groupsListModel.clear();
        dmsListModel.clear();
    }

    public void showPane(PaneSelector paneSelector) {
        SwingUtilities.invokeLater(() -> cardLayout.show(contentPanel, paneSelector.name()));
    }

    static private class InboxRenderer extends DefaultListCellRenderer {
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

}
