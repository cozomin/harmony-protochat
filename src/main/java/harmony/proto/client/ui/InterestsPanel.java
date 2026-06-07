package harmony.proto.client.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class InterestsPanel extends JPanel {
    private final DefaultListModel<String> popularModel = new DefaultListModel<>();
    private final JList<String> popularList = new JList<>(popularModel);

    private final DefaultListModel<String> myInterestsModel = new DefaultListModel<>();
    private final JList<String> myInterestsList = new JList<>(myInterestsModel);

    private final JButton addSelectedButton = new JButton("Add >");
    private final JButton removeSelectedButton = new JButton("< Remove");
    private final JButton nextButton = new JButton("INBOX");

    private final JTextField newInterestField = new JTextField();
    private final JButton createButton = new JButton("Create Custom");

    public InterestsPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Top Bar
        JLabel title = new JLabel("Manage Your Interests");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        JPanel topBar = new JPanel(new BorderLayout(3,3));
        topBar.add(title, BorderLayout.WEST);
        topBar.add(nextButton, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // enter Dual List Area
        JPanel dualListPanel = new JPanel(new GridLayout(1, 3, 10, 0));

        // Left side: Popular
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Popular Topics"), BorderLayout.NORTH);
        popularList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        leftPanel.add(new JScrollPane(popularList), BorderLayout.CENTER);

        // Middle side: Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(Box.createVerticalGlue());

        addSelectedButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeSelectedButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonPanel.add(addSelectedButton);
        buttonPanel.add(Box.createVerticalStrut(15));
        buttonPanel.add(removeSelectedButton);
        buttonPanel.add(Box.createVerticalGlue());

        // Right side: My Interests
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("My Topics"), BorderLayout.NORTH);
        myInterestsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        rightPanel.add(new JScrollPane(myInterestsList), BorderLayout.CENTER);

        dualListPanel.add(leftPanel);
        dualListPanel.add(buttonPanel);
        dualListPanel.add(rightPanel);

        add(dualListPanel, BorderLayout.CENTER);

        // Bottom Custom Entry Area
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.add(new JLabel("Don't see your interest?"), BorderLayout.WEST);
        bottomPanel.add(newInterestField, BorderLayout.CENTER);
        bottomPanel.add(createButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public DefaultListModel<String> getPopularModel() { return popularModel; }
    public DefaultListModel<String> getMyInterestsModel() { return myInterestsModel; }
    public java.util.List<String> getSelectedPopular() { return popularList.getSelectedValuesList(); }
    public java.util.List<String> getSelectedMine() { return myInterestsList.getSelectedValuesList(); }
    public String getNewInterestText() { return newInterestField.getText().trim(); }
    public void clearInputField() { newInterestField.setText(""); }

    public void setNextAction(ActionListener actionListener) {
        nextButton.addActionListener(actionListener);
    }
    public void setAddSelectedAction(ActionListener listener) { addSelectedButton.addActionListener(listener); }
    public void setRemoveSelectedAction(ActionListener listener) { removeSelectedButton.addActionListener(listener); }
    public void setCreateCustomAction(ActionListener listener) {
        createButton.addActionListener(listener);
        newInterestField.addActionListener(listener);
    }
}