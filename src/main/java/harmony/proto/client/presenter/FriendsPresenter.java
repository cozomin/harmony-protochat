package harmony.proto.client.presenter;

import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.friends.*;
import harmony.proto.dto.UserDTO;
import harmony.proto.dto.req.FriendOperation;
import harmony.proto.dto.res.FriendRes;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.JOptionPane.QUESTION_MESSAGE;

public class FriendsPresenter {
    private final FriendsPanel friendsView;
    private final WebSocketClient client;
    private final InboxPresenter inboxPresenter;
//    private UserDTO selectedUser;

    public FriendsPresenter(FriendsPanel friendsView, WebSocketClient client, InboxPresenter inboxPresenter) {
        this.friendsView = friendsView;
        this.client = client;
        this.inboxPresenter = inboxPresenter;

        bindNavigationActions();
        bindAllFriendsActions();
        bindPendingFriendsActions();
        bindAddFriendActions();
    }

    public FriendsPanel getFriendsView() {
        return friendsView;
    }

    private void bindNavigationActions() {
        friendsView.allButtonAction(e -> {
            friendsView.showPane(FriendsPaneSelector.ALL);
            loadFriends(FriendOperation.fetch_accepted, null);
        });

        friendsView.pendingButtonAction(e -> {
            friendsView.showPane(FriendsPaneSelector.PENDING);
            loadFriends(FriendOperation.fetch_incoming, null);
        });

        friendsView.addButtonAction(e -> {
            friendsView.showPane(FriendsPaneSelector.ADD);
        });
    }

    private void bindAllFriendsActions() {
        AllFriendsPanel allView = friendsView.getAllFriendsPanel();

        allView.createGroupButtonAction(e -> {
            List<UserDTO> selectedUsers = allView.getFriendsList().getSelectedValuesList();

            if (selectedUsers != null && !selectedUsers.isEmpty()) {

                List<String> stringUsers = new ArrayList<>();
                for (UserDTO user : selectedUsers) {
                    stringUsers.add(user.getUsername());
                }

                java.util.List<String> groupTopics = new java.util.ArrayList<>();

                JTextField nameField = new JTextField(15);
                JTextField topicField = new JTextField(15);

                nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, nameField.getPreferredSize().height));
                topicField.setMaximumSize(new Dimension(Integer.MAX_VALUE, topicField.getPreferredSize().height));

                JPanel tagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
                tagsPanel.setPreferredSize(new Dimension(300, 60));

                topicField.addActionListener(e1 -> {
                    String topic = topicField.getText().trim();

                    if (topic.isEmpty()) return;

                    if (topic.contains(" ")) {
                        JOptionPane.showMessageDialog(null, "Topics cannot contain spaces! Use single words.", "Invalid Topic", JOptionPane.WARNING_MESSAGE);
                    } else if (!groupTopics.contains(topic)) {
                        groupTopics.add(topic);

                        JLabel tagLabel = new JLabel("#" + topic);
                        tagLabel.setOpaque(true);
                        tagLabel.setBackground(new Color(220, 220, 220));

                        tagLabel.setForeground(Color.BLACK);
                        tagLabel.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));

                        tagsPanel.add(tagLabel);
                        tagsPanel.revalidate();
                        tagsPanel.repaint();

                        topicField.setText("");
                    } else {
                        topicField.setText("");
                    }
                });

                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

                JLabel nameLabel = new JLabel("Group Name:");
                JLabel topicLabel = new JLabel("Add Topic:");

                nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
                topicLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                topicField.setAlignmentX(Component.LEFT_ALIGNMENT);
                tagsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                panel.add(nameLabel);
                panel.add(nameField);
                panel.add(Box.createVerticalStrut(10));
                panel.add(topicLabel);
                panel.add(topicField);
                panel.add(Box.createVerticalStrut(5));
                panel.add(tagsPanel);

                int result = JOptionPane.showConfirmDialog(allView, panel,
                        "Create New Group", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    String groupName = nameField.getText().trim();

                    if (!groupName.isEmpty()) {
                        try {
                            client.createGroup(groupName, groupTopics, stringUsers);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        JOptionPane.showMessageDialog(allView, "Group Name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        allView.removeButtonAction(e -> {
            List<UserDTO> selectedUsers = allView.getFriendsList().getSelectedValuesList();
            if (selectedUsers != null) {
                for (UserDTO user : selectedUsers)
                    processFriend(FriendOperation.deny, user);
            }
            refreshAllLists();
        });

        allView.messageButtonAction(e -> {
            if(allView.getFriendsList().getSelectedIndices().length == 1) {
                UserDTO selectedUser = allView.getFriendsList().getSelectedValue();
                if (selectedUser != null) {
                    inboxPresenter.openDirectMessage(selectedUser.getUsername());
                }
            }
        });

        allView.joinGroupButtonAction(e -> {
            String groupName = JOptionPane.showInputDialog(
                    allView,
                    "Enter Server Name to Join:",
                    "Join Server",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (groupName != null && !groupName.trim().isEmpty()) {
                String cleanName = groupName.trim();

                SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return client.joinGroup(cleanName);
                    }
                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                JOptionPane.showMessageDialog(allView, "Successfully joined " + cleanName + "!", "Joined", JOptionPane.INFORMATION_MESSAGE);
                                inboxPresenter.loadChats();
                            } else {
                                JOptionPane.showMessageDialog(allView, "Could not join group. Check the name or you may already be a member.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                };
                worker.execute();
            }
        });
    }

    private void bindAddFriendActions() {
        AddFriendPanel addView = friendsView.getAddFriendPanel();

        addView.setSendRequestAction(e -> {
            String targetUsername = addView.getUsernameInput();
            if (targetUsername.isEmpty()) {
                addView.setStatusMessage("Username cannot be empty.", Color.RED);
                return;
            }

            addView.getSendRequestButton().setEnabled(false);
            addView.setStatusMessage("Sending request...", Color.GRAY);

            SwingWorker<FriendRes, Void> worker = new SwingWorker<>() {
                @Override
                protected FriendRes doInBackground() throws Exception {
                    // Send request using target username
                    return client.friendOperation(FriendOperation.send, targetUsername);
                }

                @Override
                protected void done() {
                    try {
                        FriendRes res = get();
                        if (res != null && "success".equals(res.getMessage())) {
                            addView.setStatusMessage("Friend request sent to " + targetUsername + "!", new Color(0, 150, 0));
                            addView.clearInput();
                        } else {
                            String errorMsg = res != null && res.getMessage() != null ? res.getMessage() : "Failed to send request.";
                            addView.setStatusMessage(errorMsg, Color.RED);
                        }
                    } catch (Exception ex) {
                        addView.setStatusMessage("Server error occurred.", Color.RED);
                    } finally {
                        addView.getSendRequestButton().setEnabled(true);
                    }
                }
            };
            worker.execute();
        });
    }

    private void bindPendingFriendsActions() {
        PendingFriendsPanel pendingView = friendsView.getPendingFriendsPanel();

        pendingView.acceptButtonAction(e -> {
            UserDTO selectedUser = pendingView.getPendingList().getSelectedValue();
            if (selectedUser != null) {
                processFriend(FriendOperation.accept, selectedUser);
            }
        });

        pendingView.declineButtonAction(e -> {
            UserDTO selectedUser = pendingView.getPendingList().getSelectedValue();
            if (selectedUser != null) {
                processFriend(FriendOperation.deny, selectedUser);
            }
        });
    }

    private void processFriend(FriendOperation operation, UserDTO targetUser) {
        SwingWorker<FriendRes, Void> worker = new SwingWorker<>() {
            @Override
            protected FriendRes doInBackground() throws Exception {
                return client.friendOperation(operation, targetUser.getUsername());
            }

            @Override
            protected void done() {
                try {
                    FriendRes res = get();
                    // After accepting/denying, refresh the lists to reflect changes
                    refreshAllLists();

                    if (operation == FriendOperation.accept) {
                        inboxPresenter.loadChats();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    public void refreshAllLists() {
        loadFriends(FriendOperation.fetch_accepted, null);
        loadFriends(FriendOperation.fetch_incoming, null);
    }

    public void loadFriends(FriendOperation operation, UserDTO selectedUser) {
        SwingWorker<FriendRes, Void> worker = new SwingWorker<>() {
            @Override
            protected FriendRes doInBackground() throws Exception {
                String userArg = (selectedUser != null) ? selectedUser.getUsername() : "";
                return client.friendOperation(operation, userArg);
            }

            @Override
            protected void done() {
                try {
                    FriendRes res = get();
                    if (res == null) return;

                    // Prepare the correct UI list based on operation
                    friendsView.prepareLoadFriends(res.getOperation());
                    DefaultListModel<UserDTO> targetList = friendsView.getList(res.getOperation());

                    // Populate the list
                    if (res.getUsers() != null && targetList != null) {
                        for (UserDTO user : res.getUsers()) {
                            targetList.addElement(user);
                        }
                    }

                    friendsView.setList(targetList, res.getOperation());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }


}
