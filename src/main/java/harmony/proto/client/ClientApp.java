package harmony.proto.client;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.inter.FlatInterFont;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.presenter.ClientPresenter;
import harmony.proto.client.ui.ClientUI;

import javax.swing.*;
import java.awt.*;

public class ClientApp {
    public static void main(String[] args) {
        FlatInterFont.install();
        FlatLaf.registerCustomDefaultsSource("ui/themes");
        UIManager.put("defaultFont", new Font(FlatInterFont.FAMILY, Font.PLAIN, 13));
        FlatMacDarkLaf.setup();

        //The complete Swing processing is done in a thread called EDT (Event Dispatching Thread). Therefore you would block the GUI if you would compute some long lasting calculations within this thread.
        //The event dispatch thread is a special thread that is managed by AWT. Basically, it is a thread that runs in an infinite loop, processing events.
        SwingUtilities.invokeLater(() -> {
            ClientUI ui = new ClientUI();
            WebSocketClient client = new WebSocketClient();
            new ClientPresenter(client, ui);
            ui.setVisible(true);
        });
    }
}
