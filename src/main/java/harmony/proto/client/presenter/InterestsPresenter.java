package harmony.proto.client.presenter;

import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.InterestsPanel;

import javax.swing.*;
import java.util.List;

public class InterestsPresenter {
    private final WebSocketClient client;
    private final ClientPresenter coordinator;
    private final InterestsPanel view;

    public InterestsPresenter(InterestsPanel view, WebSocketClient client, ClientPresenter coordinator) {
        this.view = view;
        this.client = client;
        this.coordinator = coordinator;
        bindActions();
    }

    private void bindActions() {
        // Move from Popular -> Mine
        view.setAddSelectedAction(e -> {
            List<String> selected = view.getSelectedPopular();
            for (String interest : selected) {
                // Ensure we don't add duplicates to the UI prematurely
                if (!view.getMyInterestsModel().contains(interest)) {
                    executeInterestOperation(interest, true);
                }
            }
        });

        // Move from Mine -> Popular (Remove)
        view.setRemoveSelectedAction(e -> {
            List<String> selected = view.getSelectedMine();
            for (String interest : selected) {
                executeInterestOperation(interest, false);
            }
        });

        // Add a custom typed interest
        view.setCreateCustomAction(e -> {
            String interest = view.getNewInterestText();
            if (!interest.isEmpty()) {
                executeInterestOperation(interest, true);
                view.clearInputField();
            }
        });

        view.setNextAction( e -> {
            try {
                coordinator.onLoginSuccess();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public void loadData() {
        loadMyInterests();
        loadPopularInterests();
    }

    private void loadMyInterests() {
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return client.fetchInterests();
            }
            @Override
            protected void done() {
                try {
                    updateListModel(view.getMyInterestsModel(), get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void loadPopularInterests() {
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return client.fetchTopPopularInterests();
            }
            @Override
            protected void done() {
                try {
                    updateListModel(view.getPopularModel(), get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void executeInterestOperation(String interest, boolean isAdding) {
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return isAdding ? client.addInterest(interest) : client.removeInterest(interest);
            }
            @Override
            protected void done() {
                try {
                    // The server returns the entire updated personal list
                    updateListModel(view.getMyInterestsModel(), get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void updateListModel(DefaultListModel<String> model, List<String> data) {
        model.clear();
        if (data != null) {
            for (String item : data) {
                model.addElement(item);
            }
        }
    }
}