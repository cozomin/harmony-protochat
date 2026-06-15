package harmony.proto.client.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;

public class LoginPanel extends JPanel {

    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private JButton bLogin;
    private final JLabel errorLabel = new JLabel();
    private final JButton bCreateAccount = createNoBorderButton("Create an account");

    URL imgUrl = getClass().getResource("/img/icons/logo.svg");

    public LoginPanel() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("wrap, insets 35 40 30 40, al center top", "[fill, grow, ::30%]"));

        try {
            FlatSVGIcon logoIcon = new FlatSVGIcon("img/icons/logo.svg", 250, 250);
            JLabel logoLabel = new JLabel(logoIcon);
            add(logoLabel, "align center, gapbottom 10");
        } catch (Exception e) {
            System.err.println("Warning: logo.svg not found.");
        }

        JLabel lbTitle = new JLabel("Welcome (back)");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +12;");
        add(lbTitle, "align center, gapbottom 5");

        JLabel lbSubtitle = new JLabel("Sign in to resonate with others");
        lbSubtitle.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground;");
        add(lbSubtitle, "align center, gapbottom 25");

        JLabel lbUsername = new JLabel("Username");
        lbUsername.putClientProperty(FlatClientProperties.STYLE, "font:bold;");
        add(lbUsername, "gapbottom 5");

        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your username");
        add(txtUsername, "gapbottom 15");

        JLabel lbPassword = new JLabel("Password");
        lbPassword.putClientProperty(FlatClientProperties.STYLE, "font:bold;");
        add(lbPassword, "gapbottom 5");

        txtPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true;");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your password");
        add(txtPassword, "gapbottom 15");

        errorLabel.setForeground(new Color(220, 80, 80));
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(errorLabel, "align center, gapbottom 10");

        bLogin = new JButton("Sign in");
        bLogin.putClientProperty(FlatClientProperties.STYLE,
                "background:$Component.accentColor;" +
                        "foreground:#FFFFFF;" +
                        "font:bold;" +
                        "margin:5,15,5,15;");

        add(bLogin, "gapbottom 20");

        JLabel lbNoAccount = new JLabel("No account?");
        lbNoAccount.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground;");

        add(lbNoAccount, "split 2, align center, gapright 5");
        add(bCreateAccount);
    }

    private JButton createNoBorderButton(String text) {
        JButton button = new JButton(text);
        button.putClientProperty(FlatClientProperties.STYLE,
                "foreground:$Component.accentColor;" +
                        "margin:1,5,1,5;" +
                        "borderWidth:0;" +
                        "focusWidth:0;" +
                        "innerFocusWidth:0;" +
                        "background:null;");
        return button;
    }

    public String getTxtUsername() {
        return txtUsername.getText().trim();
    }

    public String getTxtPassword() {
        return String.valueOf(txtPassword.getPassword());
    }

    public void setRegisterAction(ActionListener actionListener) {
        bCreateAccount.addActionListener(actionListener);
    }

    public void setLoginAction(ActionListener actionListener) {
        bLogin.addActionListener(actionListener);
        txtPassword.addActionListener(actionListener);
    }

    public void setLoginEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> bLogin.setEnabled(enabled));
    }

    public void showError(String message) {
        SwingUtilities.invokeLater(() -> errorLabel.setText(message));
    }

    public void clearError() {
        SwingUtilities.invokeLater(() -> errorLabel.setText(""));
    }

    public void clearPassword() {
        SwingUtilities.invokeLater(() -> txtPassword.setText(""));
    }
}