package harmony.proto.client.presenter;

import harmony.proto.client.PaneSelector;
import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.LoginPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.event.ActionListener;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginPresenterTest {

    @Mock private LoginPanel mockView;
    @Mock private WebSocketClient mockClient;
    @Mock private ClientPresenter mockCoordinator;

    private LoginPresenter presenter;
    private ActionListener loginActionListener;
    private ActionListener registerActionListener;

    @BeforeEach
    public void setup() {
        ArgumentCaptor<ActionListener> loginCaptor = ArgumentCaptor.forClass(ActionListener.class);
        ArgumentCaptor<ActionListener> registerCaptor = ArgumentCaptor.forClass(ActionListener.class);

        presenter = new LoginPresenter(mockView, mockClient, mockCoordinator);

        verify(mockView).setLoginAction(loginCaptor.capture());
        verify(mockView).setRegisterAction(registerCaptor.capture());

        loginActionListener = loginCaptor.getValue();
        registerActionListener = registerCaptor.getValue();
    }

    @Test
    public void testLoginWithEmptyFields_ShowsError() {
        when(mockView.getTxtUsername()).thenReturn("");
        when(mockView.getTxtPassword()).thenReturn("pass123");

        loginActionListener.actionPerformed(null);

        verify(mockView).showError("Please fill all the fields");
        verifyNoInteractions(mockClient);
    }

    @Test
    public void testLoginSuccess_NavigatesToInbox() throws Exception {
        when(mockView.getTxtUsername()).thenReturn("validUser");
        when(mockView.getTxtPassword()).thenReturn("validPass");
        when(mockClient.login("validUser", "validPass")).thenReturn(true);

        loginActionListener.actionPerformed(null);

        // Sleep to let the SwingWorker thread finish without Mockito locking it
        Thread.sleep(400);

        verify(mockView).setLoginEnabled(false);
        verify(mockCoordinator).onLoginSuccess();
        verify(mockView).setLoginEnabled(true);
    }

    @Test
    public void testLoginFailure_ShowsBackendError() throws Exception {
        when(mockView.getTxtUsername()).thenReturn("invalidUser");
        when(mockView.getTxtPassword()).thenReturn("wrongPass");
        when(mockClient.login("invalidUser", "wrongPass")).thenReturn(false);
        when(mockClient.getLoginFailureReason()).thenReturn("Invalid credentials");

        loginActionListener.actionPerformed(null);

        Thread.sleep(400);

        verify(mockView).showError("Invalid credentials");
        verify(mockCoordinator, never()).onLoginSuccess();
    }

    @Test
    public void testNavigateToRegister() {
        registerActionListener.actionPerformed(null);
        verify(mockCoordinator).navigateTo(PaneSelector.REGISTER);
    }
}