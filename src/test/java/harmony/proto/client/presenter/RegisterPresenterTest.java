package harmony.proto.client.presenter;

import harmony.proto.client.PaneSelector;
import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.RegisterPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.event.ActionListener;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegisterPresenterTest {

    @Mock private RegisterPanel mockView;
    @Mock private WebSocketClient mockClient;
    @Mock private ClientPresenter mockCoordinator;

    private RegisterPresenter presenter;
    private ActionListener registerActionListener;
    private ActionListener backToLoginListener;

    @BeforeEach
    public void setup() {
        ArgumentCaptor<ActionListener> registerCaptor = ArgumentCaptor.forClass(ActionListener.class);
        ArgumentCaptor<ActionListener> backCaptor = ArgumentCaptor.forClass(ActionListener.class);

        presenter = new RegisterPresenter(mockView, mockClient, mockCoordinator);

        verify(mockView).setRegisterAndLoginAction(registerCaptor.capture());
        verify(mockView).setBackToLogin(backCaptor.capture());

        registerActionListener = registerCaptor.getValue();
        backToLoginListener = backCaptor.getValue();
    }

    @Test
    public void testRegisterSuccess_NavigatesToInterests() throws Exception {
        when(mockView.getTxtUsername()).thenReturn("newUser");
        when(mockView.getTxtPassword()).thenReturn("securePass");
        when(mockClient.signUp("newUser", "securePass")).thenReturn(true);

        registerActionListener.actionPerformed(null);

        Thread.sleep(400);

        verify(mockCoordinator).onInterestShow();
    }

    @Test
    public void testRegisterEmptyFields_ShowsError() {
        when(mockView.getTxtUsername()).thenReturn("user");
        when(mockView.getTxtPassword()).thenReturn("");

        registerActionListener.actionPerformed(null);

        verify(mockView).showError("Please fill all the fields");
        verifyNoInteractions(mockClient);
    }

    @Test
    public void testNavigateBackToLogin() {
        backToLoginListener.actionPerformed(null);
        verify(mockCoordinator).navigateTo(PaneSelector.LOGIN);
    }
}