package harmony.proto.client.presenter;

import harmony.proto.client.PaneSelector;
import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.LoginPanel;
import harmony.proto.client.ui.RegisterPanel;

import javax.swing.*;

public class RegisterPresenter {
    private final RegisterPanel registerView;
    private final WebSocketClient client;
    private final ClientPresenter coordinator;

    public RegisterPresenter(RegisterPanel registerView, WebSocketClient client, ClientPresenter coordinator) {
        this.registerView = registerView;
        this.client = client;
        this.coordinator = coordinator;
        bind();
    }

    //binding associates a function to an ui element
    private void bind() {
        registerView.setRegisterAndLoginAction(e -> registerAndLogin());
        registerView.setBackToLogin(e -> coordinator.navigateTo(PaneSelector.LOGIN));
    }
    private void registerAndLogin() {
        registerView.clearError();

        String username = registerView.getTxtUsername();
        String password = registerView.getTxtPassword();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            registerView.showError("Please fill all the fields");
            return;
        }

        registerView.setLoginEnabled(false);
        registerView.showError("Signing in...");

        //SwingWorker is designed to handle long-running tasks in a background thread, preventing the GUI from freezing
        new SwingWorker<Boolean, Void>() {
            private String errorMessage;

            @Override
            protected Boolean doInBackground() {
                try {
                    return client.signUp(username, password);
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                registerView.setLoginEnabled(true);

                boolean success = false;
                try {
                    success = get();
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                }

                if (success) {
                    registerView.clearError();
                    try {
                        coordinator.onLoginSuccess();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    String backendMessage = client.getLoginFailureReason();
                    registerView.showError(
                            errorMessage != null && !errorMessage.isBlank()
                                    ? errorMessage
                                    : (backendMessage != null && !backendMessage.isBlank()
                                       ? backendMessage
                                       : "Login failed")
                    );
                }
            }
        }.execute();
    }
}
