package harmony.proto.client.presenter;

import harmony.proto.client.PaneSelector;
import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.LoginPanel;

import javax.swing.*;

public class LoginPresenter {
    private final LoginPanel loginView;
    private final WebSocketClient client;
    private final ClientPresenter coordinator;

    public LoginPresenter(LoginPanel loginView, WebSocketClient client, ClientPresenter coordinator) {
        this.loginView = loginView;
        this.client = client;
        this.coordinator = coordinator;
        bind();
    }

    //binding associates a backend function to an ui element
    private void bind() {
        loginView.setLoginAction(e -> login());
        loginView.setRegisterAction(e -> coordinator.navigateTo(PaneSelector.REGISTER));
    }

    private void login() {
        loginView.clearError();

        String username = loginView.getTxtUsername();
        String password = loginView.getTxtPassword();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            loginView.showError("Please fill all the fields");
            return;
        }

        loginView.setLoginEnabled(false);
        loginView.showError("Signing in...");

        //SwingWorker is designed to handle long-running tasks in a background thread, preventing the GUI from freezing
        new SwingWorker<Boolean, Void>() {
            private String errorMessage;

            @Override
            protected Boolean doInBackground() {
                try {
                    return client.login(username, password);
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                loginView.setLoginEnabled(true);

                boolean success = false;
                try {
                    success = get();
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                }

                if (success) {
                    loginView.clearError();
                    try {
                        coordinator.onLoginSuccess();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    String backendMessage = client.getLoginFailureReason();
                    loginView.showError(
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