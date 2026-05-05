package harmony.proto.client.ui;

import harmony.proto.client.PaneSelector;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class ClientUI extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    //Each section of the app uses a different screen => each screen is a different panel
    private final LoginPanel loginPanel = new LoginPanel();
    private final RegisterPanel registerPanel = new RegisterPanel();
    private final InboxPanel inboxPanel = new InboxPanel();

    //They are glued together by the cardLayout and JPanel cards

    public ClientUI() {
        init();
    }

    public void init() {
        setTitle("Harmony");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(1000, 800));
        setLocationRelativeTo(null);
        setLayout(new MigLayout("fill, insets 0", "[grow]", "[grow]"));

        //each panel needs to have a name for identification
        cards.add(loginPanel, PaneSelector.LOGIN.name());
        cards.add(registerPanel, PaneSelector.REGISTER.name());
        cards.add(inboxPanel, PaneSelector.INBOX.name());

        setContentPane(cards);
        cardLayout.show(cards, PaneSelector.LOGIN.name());
    }

    //Showing a specific panel at a time
    public void showPane(PaneSelector paneSelector) {
        SwingUtilities.invokeLater(() -> cardLayout.show(cards, paneSelector.name()));
    }

    public LoginPanel getLoginView() {
        return loginPanel;
    }

    public  RegisterPanel getRegisterView() {
        return registerPanel;
    }

    public InboxPanel getInboxView() {
        return inboxPanel;
    }

//    public static void main(String[] args) {
//        FlatInterFont.install();
//        FlatLaf.registerCustomDefaultsSource("ui/themes");
//        UIManager.put("defaultFont", new Font(FlatInterFont.FAMILY, Font.PLAIN, 13));
//        FlatMacDarkLaf.setup();
//
//        SwingUtilities.invokeLater(() -> {
//            ClientUI ui = new ClientUI();
//            WebSocketClient client = new WebSocketClient();
//            new ClientPresenter(client, ui);
//            ui.setVisible(true);
//        });
//    }
}