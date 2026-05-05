package harmony.proto.client.ui.friends;

import harmony.proto.dto.UserDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class AllFriendsPanel extends JPanel {
    private final DefaultListModel<UserDTO> friendsListModel = new DefaultListModel<>();
    private final JList<UserDTO> friendsList = new JList<>(friendsListModel);

    private final JButton messageButton = new JButton("Message");
    private final JButton removeButton = new JButton("Remove Friend");

    public AllFriendsPanel() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel headerLabel = new JLabel("All Friends");
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 16f));

        friendsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        friendsList.setCellRenderer(new FriendsListRenderer());

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.add(messageButton);
        bottomBar.add(removeButton);

        add(headerLabel, BorderLayout.NORTH);
        add(new JScrollPane(friendsList), BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    public void messageButtonAction(ActionListener actionListener) {
        messageButton.addActionListener(actionListener);
    }

    public void removeButtonAction(ActionListener actionListener) {
        removeButton.addActionListener(actionListener);
    }

    public DefaultListModel<UserDTO> getFriendsListModel() { return friendsListModel; }
    public JList<UserDTO> getFriendsList() { return friendsList; }

}

class FriendsListRenderer extends  DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus
    ) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof UserDTO) {
            setText(((UserDTO) value).getUsername());
        }
        return this;
    }
}