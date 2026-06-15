package harmony.proto.client.ui;

import harmony.proto.client.PaneSelector;
import harmony.proto.client.ui.friends.FriendsPanel;
import harmony.proto.dto.ChatDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InboxPanel extends JPanel {
    private final ChatPanel chatPanel = new ChatPanel();
    private final FriendsPanel friendsPanel = new FriendsPanel();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final JButton interestsButton = new JButton("Interests");
    private final JButton loadButton = new JButton("Refresh");
    private final JButton logoutButton = new JButton("Logout");
    private final JButton friendsButton = new JButton("Friends");

    private final DefaultListModel<ChatDTO> groupsListModel = new DefaultListModel<>();
    private final DefaultListModel<ChatDTO> dmsListModel = new DefaultListModel<>();
    private final DefaultListModel<String> membersListModel = new DefaultListModel<>();

    private final DefaultListModel<String> topicsListModel = new DefaultListModel<>();

    private final JList<ChatDTO> groupsList = new JList<>(groupsListModel);
    private final JList<ChatDTO> dmsList = new JList<>(dmsListModel);
    private final JList<String> membersList = new JList<>(membersListModel);

    private final JList<String> topicsList = new JList<>(topicsListModel);

    private JSplitPane leftSplitPane;
    private JSplitPane rightSplitPane;
    private final int originalDividerSize = 10;

    JPanel rightSidebar = new JPanel();
    JPanel topicsPanel = new JPanel();
    JPanel groupMembersPanel = new JPanel();
    JPopupMenu sidePopupMenu = new JPopupMenu();
    JMenuItem memberListButton = new JMenuItem("Member list");
    JMenuItem leaveButton = new JMenuItem("Leave");

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
        topBar.add(friendsButton);
        topBar.add(interestsButton);
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

        // Right Sidebar
        rightSidebar.setLayout(new BoxLayout(rightSidebar, BoxLayout.Y_AXIS));
        rightSidebar.setPreferredSize(new Dimension(320, 0));

        JLabel groupMembersLabel = new JLabel("Group members");

        sidePopupMenu.add(memberListButton);
        sidePopupMenu.add(leaveButton);

        memberListButton.addActionListener(e -> toggleGroupMembers(true));

        groupsList.setComponentPopupMenu(sidePopupMenu);

        // Members Panel
        groupMembersPanel.setLayout(new BoxLayout(groupMembersPanel, BoxLayout.Y_AXIS));
        groupMembersPanel.setPreferredSize(new Dimension(320, 0));
        groupMembersPanel.add(groupMembersLabel);
        groupMembersPanel.add(new JScrollPane(membersList));

        // Topics Panel
        topicsPanel.setLayout(new BoxLayout(topicsPanel, BoxLayout.Y_AXIS));
        topicsPanel.setPreferredSize(new Dimension(320, 0));

        JLabel topicsLabel = new JLabel("Group Topics");
        topicsLabel.setBorder(new EmptyBorder(10, 0, 5, 0));

        topicsPanel.add(topicsLabel);
        topicsPanel.add(new JScrollPane(topicsList));

        leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sideBar, contentPanel);
        leftSplitPane.setDividerLocation(200);
        leftSplitPane.setDividerSize(originalDividerSize);
        leftSplitPane.setContinuousLayout(true);

        sideBar.setMinimumSize(new Dimension(0, 0));
        contentPanel.setMinimumSize(new Dimension(300, 0));

        leftSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
            int currentPos = leftSplitPane.getDividerLocation();
            int maxPos = leftSplitPane.getMaximumDividerLocation();
            int threshold = 30;

            if (currentPos > 0 && currentPos < threshold) {
                leftSplitPane.setDividerLocation(0);
            }
            else if (maxPos > 0 && currentPos > (maxPos - threshold)) {
                leftSplitPane.setDividerLocation(maxPos);
            }
        });

        rightSidebar.add(groupMembersPanel);
        rightSidebar.add(topicsPanel);

        rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplitPane, null);
        rightSplitPane.setDividerSize(0);
        rightSplitPane.setContinuousLayout(true);
        rightSidebar.setMinimumSize(new Dimension(0, 0));

        rightSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
            if (rightSplitPane.getRightComponent() == null) return;

            int currentPos = rightSplitPane.getDividerLocation();
            int maxPos = rightSplitPane.getMaximumDividerLocation();
            int threshold = 25;

            if (maxPos > 0 && currentPos > (maxPos - threshold)) {
                toggleGroupMembers(false);
            }
        });

        add(topBar, BorderLayout.NORTH);
        add(rightSplitPane, BorderLayout.CENTER);

        toggleGroupMembers(false);
        cardLayout.show(contentPanel, PaneSelector.FRIENDS.name());
    }

    public void toggleGroupMembers(boolean visible) {
        if (visible) {
            if (rightSplitPane.getRightComponent() == rightSidebar) return;

            rightSplitPane.setRightComponent(rightSidebar);
            rightSplitPane.setDividerSize(originalDividerSize);

            int currentWidth = getWidth() > 0 ? getWidth() : 800;
            rightSplitPane.setDividerLocation(currentWidth - 200);
        }
        else {
            if (rightSplitPane.getRightComponent() == null) return;

            rightSplitPane.setRightComponent(null);
            rightSplitPane.setDividerSize(0);
        }

        rightSplitPane.revalidate();
        rightSplitPane.repaint();
    }

    public JList<ChatDTO> getDmsList() { return dmsList; }
    public JList<ChatDTO> getGroupsList() { return groupsList; }
    public JList<String> getMembersList() { return membersList; }
    public DefaultListModel<ChatDTO> getGroupsListModel() { return groupsListModel; }
    public DefaultListModel<ChatDTO> getDmsListModel() { return dmsListModel; }
    public DefaultListModel<String> getMembersListModel() { return membersListModel; }

    public JList<String> getTopicsList() { return topicsList; }
    public DefaultListModel<String> getTopicsListModel() { return topicsListModel; }

    public ChatPanel getChatView() { return chatPanel; }
    public FriendsPanel getFriendsPanel() { return friendsPanel; }

    public void setFriendsButtonAction(ActionListener actionListener) { friendsButton.addActionListener(actionListener); }
    public void setInterestsButtonAction(ActionListener actionListener) { interestsButton.addActionListener(actionListener); }
    public void setLogoutAction(ActionListener actionListener) { logoutButton.addActionListener(actionListener); }
    public void setGroupListAction(ListSelectionListener listSelectionListener) { groupsList.addListSelectionListener(listSelectionListener); }
    public void setDmsListAction(ListSelectionListener listSelectionListener) { dmsList.addListSelectionListener(listSelectionListener); }
    public void setLoadAction(ActionListener actionListener) { loadButton.addActionListener(actionListener); }
    public void setMemberListAction(ActionListener actionListener) { memberListButton.addActionListener(actionListener); }
    public void setLeaveButtonAction(ActionListener actionListener) { leaveButton.addActionListener(actionListener); }
    public void prepareLoadChats() {
        groupsListModel.clear();
        dmsListModel.clear();
    }

    public void prepareLoadChatMembers(){
        membersListModel.clear();
    }

    public void prepareLoadTopics() {
        topicsListModel.clear();
    }

    public void showPane(PaneSelector paneSelector) {
        SwingUtilities.invokeLater(() -> cardLayout.show(contentPanel, paneSelector.name()));
    }

    public void hideGroupMembersPanel() {
        toggleGroupMembers(false);
    }

    public void showGroupMembersPanel() {
        toggleGroupMembers(true);
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