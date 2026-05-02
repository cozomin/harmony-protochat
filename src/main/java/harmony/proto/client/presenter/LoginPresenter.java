package harmony.proto.client.presenter;

import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.LoginPanel;

import javax.swing.*;

public class LoginPresenter {
    private final LoginPanel view;
    private final WebSocketClient client;
    private final ClientPresenter coordinator;

    public LoginPresenter(LoginPanel view, WebSocketClient client, ClientPresenter coordinator) {
        this.view = view;
        this.client = client;
        this.coordinator = coordinator;
        bind();
    }

    //binding associates a function to an ui element
    private void bind() {
        view.setLoginAction(e -> login());
    }

    private void login() {
        view.clearError();

        String username = view.getTxtUsername();
        String password = view.getTxtPassword();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            view.showError("Please fill all the fields");
            return;
        }

        view.setLoginEnabled(false);
        view.showError("Signing in...");

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
                view.setLoginEnabled(true);

                boolean success = false;
                try {
                    success = get();
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                }

                if (success) {
                    view.clearError();
                    try {
                        coordinator.onLoginSuccess();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    String backendMessage = client.getLoginFailureReason();
                    view.showError(
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