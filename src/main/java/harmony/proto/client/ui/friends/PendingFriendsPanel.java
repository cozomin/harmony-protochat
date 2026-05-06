package harmony.proto.client.ui.friends;

import harmony.proto.dto.UserDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class PendingFriendsPanel extends JPanel {
    private DefaultListModel<UserDTO> pendingListModel = new DefaultListModel<>();
    private final JList<UserDTO> pendingList = new JList<>(pendingListModel);

    private final JButton acceptButton = new JButton("Accept");
    private final JButton declineButton = new JButton("Decline");

    public PendingFriendsPanel() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel headerLabel = new JLabel("Pending Friend Requests");
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 16f));

        pendingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pendingList.setCellRenderer(new FriendsListRenderer());

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.add(acceptButton);
        bottomBar.add(declineButton);

        add(headerLabel, BorderLayout.NORTH);
        add(new JScrollPane(pendingList), BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    public void acceptButtonAction(ActionListener actionListener) {
        acceptButton.addActionListener(actionListener);
    }

    public void declineButtonAction(ActionListener actionListener) {
        declineButton.addActionListener(actionListener);
    }

    public DefaultListModel<UserDTO> getPendingListModel() { return pendingListModel; }
    public JList<UserDTO> getPendingList() { return pendingList; }

    public void setList(DefaultListModel<UserDTO> listModel) {
        this.pendingListModel = listModel;
    }
}