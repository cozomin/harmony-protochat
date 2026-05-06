package harmony.proto.client.ui.friends;

import harmony.proto.dto.UserDTO;
import harmony.proto.dto.req.FriendOperation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class FriendsPanel extends JPanel {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final JButton allButton = new JButton("All");
    private final JButton pendingButton = new JButton("Pending");
    private final JButton addButton = new JButton("Add Friend");

    private final AllFriendsPanel allFriendsPanel = new AllFriendsPanel();
    private final PendingFriendsPanel pendingFriendsPanel = new PendingFriendsPanel();
    private final AddFriendPanel addFriendPanel = new AddFriendPanel();

    public FriendsPanel() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.add(allButton);
        topBar.add(pendingButton);
        topBar.add(addButton);

        contentPanel.add(allFriendsPanel, FriendsPaneSelector.ALL.name());
        contentPanel.add(pendingFriendsPanel, FriendsPaneSelector.PENDING.name());
        contentPanel.add(addFriendPanel, FriendsPaneSelector.ADD.name());

        add(topBar, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        cardLayout.show(contentPanel, FriendsPaneSelector.ALL.name());
    }

    public void allButtonAction(ActionListener actionListener) {
        allButton.addActionListener(actionListener);
    }

    public void pendingButtonAction(ActionListener actionListener) {
        pendingButton.addActionListener(actionListener);
    }

    public void addButtonAction(ActionListener actionListener) {
        addButton.addActionListener(actionListener);
    }

    public void prepareLoadFriends(FriendOperation operation) {
        if (operation == null) return;

        switch (operation) {
            case fetch_accepted:
                allFriendsPanel.getFriendsListModel().clear();
                break;
            case fetch_incoming, fetch_outgoing:
                pendingFriendsPanel.getPendingListModel().clear();
                break;
            default:
                break;
        }
    }

    public void setList(DefaultListModel<UserDTO> listModel, FriendOperation operation) {
        if (operation == FriendOperation.fetch_accepted) {
            allFriendsPanel.setList(listModel);
        } else if (operation == FriendOperation.fetch_incoming || operation == FriendOperation.fetch_outgoing) {
            pendingFriendsPanel.setList(listModel);
        }
    }

    public DefaultListModel<UserDTO> getList(FriendOperation operation) {
        if (operation == FriendOperation.fetch_accepted) {
            return allFriendsPanel.getFriendsListModel();
        } else if (operation == FriendOperation.fetch_incoming || operation == FriendOperation.fetch_outgoing) {
            return pendingFriendsPanel.getPendingListModel();
        }
        return null;
    }

    public AllFriendsPanel getAllFriendsPanel() {
        return allFriendsPanel;
    }

    public PendingFriendsPanel getPendingFriendsPanel() {
        return pendingFriendsPanel;
    }

    public AddFriendPanel getAddFriendPanel() {
        return addFriendPanel;
    }

    public void showPane(FriendsPaneSelector selector){
        cardLayout.show(contentPanel, selector.name());
    }
}
