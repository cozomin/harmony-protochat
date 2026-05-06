package harmony.proto.client.presenter;

import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.friends.*;
import harmony.proto.dto.UserDTO;
import harmony.proto.dto.req.FriendOperation;
import harmony.proto.dto.res.FriendRes;

import javax.swing.*;
import java.awt.*;
import java.util.List;

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

        allView.removeButtonAction(e -> {
            UserDTO selectedUser = allView.getFriendsList().getSelectedValue();
            if (selectedUser != null) {
                processFriend(FriendOperation.deny, selectedUser);
            }
            refreshAllLists();
        });

        allView.messageButtonAction(e -> {
            UserDTO selectedUser = allView.getFriendsList().getSelectedValue();
            if (selectedUser != null) {
                inboxPresenter.openDirectMessage(selectedUser.getUsername());
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
