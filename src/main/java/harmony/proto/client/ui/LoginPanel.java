package harmony.proto.client.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel {

    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private JButton bLogin;
    private final JLabel errorLabel = new JLabel();
    JButton bCreateAccount = createNoBorderButton("Create an account");

    public LoginPanel() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("wrap 1, al center center", "[fill]"));
//        setLayout(new BorderLayout());
//        setBorder(new EmptyBorder(20, 20, 20, 20));

//        add(new JLabel(new FlatSVGIcon("login/icon/logo.svg", 1.5f)));

//        JLabel lbHarmony = new JLabel("Harmony", JLabel.CENTER);
//        lbHarmony.putClientProperty(FlatClientProperties.STYLE, "font:bold +25;");
//        lbHarmony.setHorizontalAlignment(SwingConstants.CENTER);
//        add(lbHarmony, "gapy 12 220");

        JLabel lbTitle = new JLabel("Welcome (back)", JLabel.CENTER);
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +15;");
        add(lbTitle, "gapy 12 12");
//        add(lbTitle, BorderLayout.NORTH);

        add(new JLabel("Sign in to resonate with others", JLabel.CENTER));

        JLabel lbUsername = new JLabel("Username");
        lbUsername.putClientProperty(FlatClientProperties.STYLE, "font:bold;");
        add(lbUsername, "gapy 10 5");
//        add(lbUsername, BorderLayout.CENTER);

//        txtUsername.putClientProperty(FlatClientProperties.STYLE, "iconTextGap:10;");
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your username");
//        txtUsername.putClientProperty(
//                FlatClientProperties.TEXT_FIELD_LEADING_ICON,
//                new FlatSVGIcon("login/icon/email.svg", 0.35f)
//        );
        add(txtUsername);

        JLabel lbPassword = new JLabel("Password");
        lbPassword.putClientProperty(FlatClientProperties.STYLE, "font:bold;");
        add(lbPassword, "gapy 10 5,split 2");
//        add(lbPassword, BorderLayout.CENTER);

        JButton cmdForgotPassword = createNoBorderButton("Forgot Password ?");
        add(cmdForgotPassword, "grow 0,gapy 10 5");
//        add(cmdForgotPassword, BorderLayout.CENTER);

        txtPassword.putClientProperty(FlatClientProperties.STYLE, "iconTextGap:10;showRevealButton:true;");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your password");
//        txtPassword.putClientProperty(
//                FlatClientProperties.TEXT_FIELD_LEADING_ICON,
//                new FlatSVGIcon("login/icon/password.svg", 0.35f)
//        );
        add(txtPassword);

        add(new JCheckBox("Remember"), "gapy 10 10");

        bLogin = new JButton("Sign in");
        bLogin.putClientProperty(FlatClientProperties.STYLE, "foreground:#FFFFFF;iconTextGap:10;");
        bLogin.setHorizontalTextPosition(JButton.LEADING);
        add(bLogin, "gapy n 10");

        errorLabel.setForeground(new Color(220, 80, 80));
        add(errorLabel, "gapy n 5");

        JLabel lbNoAccount = new JLabel("No account ?");
        lbNoAccount.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground;");
        add(lbNoAccount, "split 2,gapx push n");

        add(bCreateAccount, "gapx n push");
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