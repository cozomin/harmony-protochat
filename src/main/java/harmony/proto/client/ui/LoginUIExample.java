package harmony.proto.client.ui;


import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class LoginUIExample extends JPanel {
    public LoginUIExample() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("fill, insets 20", "[center]", "[center]"));
        textUsername = new JTextField();
        textPassword = new JPasswordField();
        chRemember = new JCheckBox("Remember");
        bLogin = new JButton("Login");
        JPanel panel = new JPanel(new MigLayout("wrap, fillx, insets 35 45 30 45", "fill, 250:2800"));
        panel.putClientProperty(FlatClientProperties.STYLE,"" +
                "arc: 20;" +
                "[light]background:darken(@background, 3%);" +
                "[dark]background:lighten(@background, 3%);");

        textPassword.putClientProperty(FlatClientProperties.STYLE,"" +
                "showRevealButton: true");

        bLogin.putClientProperty(FlatClientProperties.STYLE, "" +
                "[light]background:darken(@background, 10%);" +
                "[dark]background:lighten(@background, 10%);" +
                "borderWidth: 0;" +
                "focusWidth: 0;" +
                "innerFocusWidth: 0;");

        textUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Enter your username or email");
        textPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Enter your password");

        JLabel lbTitle = new JLabel("Welcome!");
        JLabel lbDescription = new JLabel("Please sign in to access your account");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:bold +10;");

//        lbDescription.setHorizontalAlignment(SwingConstants.CENTER);
//        lbDescription.setVerticalAlignment(SwingConstants.CENTER);
        lbDescription.putClientProperty(FlatClientProperties.STYLE, "" +
                "[light]background:darken(@foreground, 30%);" +
                "[dark]background:lighten(@foreground, 30%);");

        panel.add(lbTitle);
        panel.add(lbDescription);

        panel.add(new JLabel("Username:"), "gapy 8");
        panel.add(textUsername);
        panel.add(new JLabel("Password:"), "gapy 8");
        panel.add(textPassword);
        panel.add(chRemember, "grow 0");
        panel.add(bLogin, "gapy 10");
        panel.add(createSignupLabel(), "gapy 10");

        add(panel);
    }

    private Component createSignupLabel(){
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
        panel.putClientProperty(FlatClientProperties.STYLE,"" +
                "background: null");

        JButton btnRegister = new JButton("Signup");
        btnRegister.putClientProperty(FlatClientProperties.STYLE, "" +
                "border: 3, 3, 3, 3");
        btnRegister.setContentAreaFilled(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(e -> {
            System.out.println("Go sign up");
        });
        JLabel label = new JLabel("Don't have an account yet?");
//        label.putClientProperty(FlatClientProperties.STYLE, "" +
//            "[light] background:lighten(@foreground, 30%);" +
//            "[dark] background:darken(@foreground, 30%);");
        panel.add(label);
        panel.add(btnRegister);

        return panel;
    }

    private JTextField textUsername = new JTextField();
    private JPasswordField textPassword = new JPasswordField();
    private JCheckBox chRemember = new JCheckBox("Remember");
    private JButton bLogin = new JButton("Login");
}
