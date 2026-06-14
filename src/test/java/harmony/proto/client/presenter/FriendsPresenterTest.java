package harmony.proto.client.presenter;

import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.friends.AddFriendPanel;
import harmony.proto.client.ui.friends.AllFriendsPanel;
import harmony.proto.client.ui.friends.FriendsPanel;
import harmony.proto.client.ui.friends.FriendsPaneSelector;
import harmony.proto.client.ui.friends.PendingFriendsPanel;
import harmony.proto.dto.res.FriendRes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.event.ActionListener;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FriendsPresenterTest {

    @Mock private FriendsPanel mockFriendsView;
    @Mock private WebSocketClient mockClient;
    @Mock private InboxPresenter mockInboxPresenter;

    @Mock private AllFriendsPanel mockAllFriendsPanel;
    @Mock private PendingFriendsPanel mockPendingFriendsPanel;
    @Mock private AddFriendPanel mockAddFriendPanel;

    private FriendsPresenter presenter;
    private ActionListener allButtonListener;
    private ActionListener pendingButtonListener;

    @BeforeEach
    public void setup() {
        ArgumentCaptor<ActionListener> allCaptor = ArgumentCaptor.forClass(ActionListener.class);
        ArgumentCaptor<ActionListener> pendingCaptor = ArgumentCaptor.forClass(ActionListener.class);

        lenient().when(mockFriendsView.getAllFriendsPanel()).thenReturn(mockAllFriendsPanel);
        lenient().when(mockFriendsView.getPendingFriendsPanel()).thenReturn(mockPendingFriendsPanel);
        lenient().when(mockFriendsView.getAddFriendPanel()).thenReturn(mockAddFriendPanel);

        presenter = new FriendsPresenter(mockFriendsView, mockClient, mockInboxPresenter);

        verify(mockFriendsView).allButtonAction(allCaptor.capture());
        verify(mockFriendsView).pendingButtonAction(pendingCaptor.capture());

        allButtonListener = allCaptor.getValue();
        pendingButtonListener = pendingCaptor.getValue();
    }

    @Test
    public void testAllButton_SwitchesPaneAndLoadsData() throws Exception {

        lenient().when(mockClient.friendOperation(eq(harmony.proto.dto.req.FriendOperation.fetch_accepted), anyString()))
                .thenReturn(new FriendRes(harmony.proto.dto.req.FriendOperation.fetch_accepted, Collections.emptyList()));

        allButtonListener.actionPerformed(null);

        Thread.sleep(400);

        verify(mockFriendsView).showPane(FriendsPaneSelector.ALL);
        verify(mockClient).friendOperation(eq(harmony.proto.dto.req.FriendOperation.fetch_accepted), anyString());
    }

    @Test
    public void testPendingButton_SwitchesPaneAndLoadsData() throws Exception {
        lenient().when(mockClient.friendOperation(eq(harmony.proto.dto.req.FriendOperation.fetch_incoming), anyString()))
                .thenReturn(new FriendRes(harmony.proto.dto.req.FriendOperation.fetch_incoming, Collections.emptyList()));

        pendingButtonListener.actionPerformed(null);

        Thread.sleep(400);

        verify(mockFriendsView).showPane(FriendsPaneSelector.PENDING);

        verify(mockClient).friendOperation(eq(harmony.proto.dto.req.FriendOperation.fetch_incoming), anyString());
    }
}