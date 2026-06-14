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
                view.getAiRecommendedModel().clear();
                coordinator.onLoginSuccess();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        view.getMyInterestsList().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedTopic = view.getMyInterestsList().getSelectedValue();
                if (selectedTopic != null) {
                    view.getAiRecommendedModel().clear();
                    view.getAiRecommendedModel().addElement("Loading AI Recommendations...");
                    fetchAIRecommendations(selectedTopic);
                }
            }
        });

        view.setJoinSuggestedAction(e -> {
            String selected = view.getSelectedSuggestedServer();

            if (selected != null && selected.startsWith("#")) {
                String groupName = selected.substring(1).trim();

                SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return client.joinGroup(groupName);
                    }
                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                JOptionPane.showMessageDialog(view, "Successfully joined " + groupName + "!", "Joined", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(view, "Could not join group. You might already be a member.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                };
                worker.execute();
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

    private void fetchAIRecommendations(String topic) {
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return client.getAIRecommendedGroups(topic);
            }
            @Override
            protected void done() {
                try {
                    List<String> recommendations = get();
                    DefaultListModel<String> model = view.getAiRecommendedModel();
                    model.clear();

                    if (recommendations != null && !recommendations.isEmpty() && !recommendations.get(0).isEmpty()) {
                        for (String group : recommendations) {
                            model.addElement("#" + group);
                        }
                    } else {
                        model.addElement("No matching servers found for this topic right now.");
                    }
                } catch (Exception e) {
                    view.getAiRecommendedModel().clear();
                    view.getAiRecommendedModel().addElement("Error fetching recommendations.");
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
}