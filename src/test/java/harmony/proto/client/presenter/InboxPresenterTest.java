package harmony.proto.client.presenter;

import harmony.proto.client.PaneSelector;
import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.ChatPanel;
import harmony.proto.client.ui.InboxPanel;
import harmony.proto.client.ui.friends.AddFriendPanel;
import harmony.proto.client.ui.friends.AllFriendsPanel;
import harmony.proto.client.ui.friends.FriendsPanel;
import harmony.proto.client.ui.friends.PendingFriendsPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.JList;
import java.awt.event.ActionListener;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InboxPresenterTest {

    @Mock private InboxPanel mockInboxView;
    @Mock private WebSocketClient mockClient;
    @Mock private ClientPresenter mockCoordinator;

    @Mock private ChatPanel mockChatPanel;
    @Mock private FriendsPanel mockFriendsPanel;
    @Mock private JList mockDmsList;
    @Mock private JList mockGroupsList;

    @Mock private AllFriendsPanel mockAllFriendsPanel;
    @Mock private PendingFriendsPanel mockPendingFriendsPanel;
    @Mock private AddFriendPanel mockAddFriendPanel;

    private InboxPresenter presenter;
    private ActionListener friendsButtonListener;
    private ActionListener logoutButtonListener;

    @BeforeEach
    public void setup() {
        when(mockInboxView.getChatView()).thenReturn(mockChatPanel);
        when(mockInboxView.getFriendsPanel()).thenReturn(mockFriendsPanel);

        // We use lenient() so Mockito doesn't throw exceptions if they aren't explicitly verified
        lenient().when(mockFriendsPanel.getAllFriendsPanel()).thenReturn(mockAllFriendsPanel);
        lenient().when(mockFriendsPanel.getPendingFriendsPanel()).thenReturn(mockPendingFriendsPanel);
        lenient().when(mockFriendsPanel.getAddFriendPanel()).thenReturn(mockAddFriendPanel);

        ArgumentCaptor<ActionListener> friendsBtnCaptor = ArgumentCaptor.forClass(ActionListener.class);
        ArgumentCaptor<ActionListener> logoutBtnCaptor = ArgumentCaptor.forClass(ActionListener.class);

        presenter = new InboxPresenter(mockInboxView, mockClient, mockCoordinator);

        verify(mockInboxView).setFriendsButtonAction(friendsBtnCaptor.capture());
        verify(mockInboxView).setLogoutAction(logoutBtnCaptor.capture());

        friendsButtonListener = friendsBtnCaptor.getValue();
        logoutButtonListener = logoutBtnCaptor.getValue();
    }

    @Test
    public void testFriendsButton_ClearsSelectionAndNavigates() {
        when(mockInboxView.getDmsList()).thenReturn(mockDmsList);
        when(mockInboxView.getGroupsList()).thenReturn(mockGroupsList);

        friendsButtonListener.actionPerformed(null);

        verify(mockDmsList).clearSelection();
        verify(mockGroupsList).clearSelection();
        verify(mockInboxView).hideGroupMembersPanel();
        verify(mockInboxView, atLeastOnce()).showPane(PaneSelector.FRIENDS);
    }

    @Test
    public void testLogoutButton_DisconnectsAndNotifiesCoordinator() throws Exception {
        logoutButtonListener.actionPerformed(null);

        verify(mockClient).disconnect();
        verify(mockCoordinator).onLogout();
    }
}