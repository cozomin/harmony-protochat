package harmony.proto.client.presenter;

import harmony.proto.client.PaneSelector;
import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.*;
import harmony.proto.client.ui.friends.AddFriendPanel;
import harmony.proto.client.ui.friends.AllFriendsPanel;
import harmony.proto.client.ui.friends.FriendsPanel;
import harmony.proto.client.ui.friends.PendingFriendsPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.JList;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientPresenterTest {

    @Mock private WebSocketClient mockClient;
    @Mock private ClientUI mockMainView;

    @Mock private LoginPanel mockLoginView;
    @Mock private RegisterPanel mockRegisterView;
    @Mock private InboxPanel mockInboxView;
    @Mock private InterestsPanel mockInterestsView;
    @Mock private ChatPanel mockChatPanel;

    // Friends-related panels
    @Mock private FriendsPanel mockFriendsPanel;
    @Mock private AddFriendPanel mockAddFriendPanel;
    @Mock private AllFriendsPanel mockAllFriendsPanel;
    @Mock private PendingFriendsPanel mockPendingFriendsPanel;

    private ClientPresenter presenter;

    @BeforeEach
    public void setup() {
        // Mock all top-level UI components accessed by the coordinator
        when(mockMainView.getLoginView()).thenReturn(mockLoginView);
        when(mockMainView.getRegisterView()).thenReturn(mockRegisterView);
        when(mockMainView.getInboxView()).thenReturn(mockInboxView);
        when(mockMainView.getInterestsView()).thenReturn(mockInterestsView);

        // Mock Inbox sub-panels
        when(mockInboxView.getChatView()).thenReturn(mockChatPanel);
        when(mockInboxView.getFriendsPanel()).thenReturn(mockFriendsPanel);

        lenient().when(mockFriendsPanel.getAllFriendsPanel()).thenReturn(mockAllFriendsPanel);
        lenient().when(mockFriendsPanel.getPendingFriendsPanel()).thenReturn(mockPendingFriendsPanel);
        lenient().when(mockFriendsPanel.getAddFriendPanel()).thenReturn(mockAddFriendPanel);

        // The InterestsPresenter adds listeners to this list in its constructor!
        lenient().when(mockInterestsView.getMyInterestsList()).thenReturn(mock(JList.class));

        presenter = new ClientPresenter(mockClient, mockMainView);
    }

    @Test
    public void testOnLogout_ClearsDataAndNavigatesToLogin() {
        // Act
        presenter.onLogout();

        // Assert
        verify(mockLoginView).clearPassword();
        verify(mockRegisterView).clearPassword();
        verify(mockAddFriendPanel).clearStatusMessage();
        verify(mockAddFriendPanel).clearInput();

        verify(mockMainView, atLeastOnce()).showPane(PaneSelector.LOGIN);
    }

    @Test
    public void testNavigateTo_ChangesPane() {
        // Act
        presenter.navigateTo(PaneSelector.REGISTER);

        // Assert
        verify(mockMainView, atLeastOnce()).showPane(PaneSelector.REGISTER);
    }
}