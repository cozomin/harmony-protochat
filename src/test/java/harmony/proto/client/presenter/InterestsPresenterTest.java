package harmony.proto.client.presenter;

import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.InterestsPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.DefaultListModel;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InterestsPresenterTest {

    @Mock private InterestsPanel mockView;
    @Mock private WebSocketClient mockClient;
    @Mock private ClientPresenter mockCoordinator;

    // We must use Mocks for the models so we can use verify() on them
    @Mock private DefaultListModel<String> mockMyInterestsModel;
    @Mock private DefaultListModel<String> mockAiRecommendedModel;
    @Mock private DefaultListModel<String> mockPopularModel;

    private InterestsPresenter presenter;
    private ActionListener addSelectedListener;
    private ActionListener removeSelectedListener;
    private ActionListener createCustomListener;
    private ActionListener nextListener;

    @BeforeEach
    public void setup() {
        ArgumentCaptor<ActionListener> addCaptor = ArgumentCaptor.forClass(ActionListener.class);
        ArgumentCaptor<ActionListener> removeCaptor = ArgumentCaptor.forClass(ActionListener.class);
        ArgumentCaptor<ActionListener> createCustomCaptor = ArgumentCaptor.forClass(ActionListener.class);
        ArgumentCaptor<ActionListener> nextCaptor = ArgumentCaptor.forClass(ActionListener.class);

        // Map the mocked UI models to the View methods
        lenient().when(mockView.getMyInterestsModel()).thenReturn(mockMyInterestsModel);
        lenient().when(mockView.getAiRecommendedModel()).thenReturn(mockAiRecommendedModel);
        lenient().when(mockView.getPopularModel()).thenReturn(mockPopularModel);
        lenient().when(mockView.getMyInterestsList()).thenReturn(mock(javax.swing.JList.class));

        presenter = new InterestsPresenter(mockView, mockClient, mockCoordinator);

        verify(mockView).setAddSelectedAction(addCaptor.capture());
        verify(mockView).setRemoveSelectedAction(removeCaptor.capture());
        verify(mockView).setCreateCustomAction(createCustomCaptor.capture());
        verify(mockView).setNextAction(nextCaptor.capture());

        addSelectedListener = addCaptor.getValue();
        removeSelectedListener = removeCaptor.getValue();
        createCustomListener = createCustomCaptor.getValue();
        nextListener = nextCaptor.getValue();
    }

    @Test
    public void testAddSelectedInterest_TriggersBackendAddInterest() throws Exception {
        // Arrange
        when(mockView.getSelectedPopular()).thenReturn(Arrays.asList("Gaming"));

        when(mockMyInterestsModel.contains("Gaming")).thenReturn(false);
        when(mockClient.addInterest("Gaming")).thenReturn(Arrays.asList("Gaming"));

        // Act
        addSelectedListener.actionPerformed(null);

        // Let the test yield execution so SwingWorker can run without Mockito locking it
        Thread.sleep(400);

        // Assert
        verify(mockClient).addInterest("Gaming");
    }

    @Test
    public void testRemoveSelectedInterest_TriggersBackendRemoveInterest() throws Exception {
        // Arrange
        when(mockView.getSelectedMine()).thenReturn(Arrays.asList("Technology"));
        when(mockClient.removeInterest("Technology")).thenReturn(Collections.emptyList());

        // Act
        removeSelectedListener.actionPerformed(null);

        Thread.sleep(400);

        // Assert
        verify(mockClient).removeInterest("Technology");
    }

    @Test
    public void testCreateCustomInterest_TriggersBackendAddInterest() throws Exception {
        // Arrange
        when(mockView.getNewInterestText()).thenReturn("Programming");
        when(mockClient.addInterest("Programming")).thenReturn(Arrays.asList("Programming"));

        // Act
        createCustomListener.actionPerformed(null);

        Thread.sleep(400);

        // Assert
        verify(mockClient).addInterest("Programming");
        verify(mockView).clearInputField();
    }

    @Test
    public void testNextAction_ClearsAIModelAndNavigatesToInbox() throws Exception {
        // Act
        nextListener.actionPerformed(null);

        // Assert
        verify(mockAiRecommendedModel).clear();
        verify(mockCoordinator).onLoginSuccess();
    }

    @Test
    public void testLoadData_FetchesInterestsFromBackend() throws Exception {
        // Arrange
        when(mockClient.fetchInterests()).thenReturn(Arrays.asList("Art"));
        when(mockClient.fetchTopPopularInterests()).thenReturn(Arrays.asList("Music", "Gaming"));

        // Act
        presenter.loadData();

        Thread.sleep(400);

        // Assert
        verify(mockClient).fetchInterests();
        verify(mockClient).fetchTopPopularInterests();
    }
}