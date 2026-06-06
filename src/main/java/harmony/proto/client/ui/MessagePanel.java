package harmony.proto.client.ui;

import harmony.proto.dto.MessageDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

public class MessagePanel extends JPanel {
    private final Long messageId;
    private final JTextArea textBody;
    private final boolean isOwnMessage;

    private final JPopupMenu popupMenu = new JPopupMenu();
    private final JMenuItem editItem = new JMenuItem("Edit");
    private final JMenuItem deleteItem = new JMenuItem("Delete");

    public MessagePanel(MessageDTO msg, boolean isOwnMessage) {
        this.isOwnMessage = isOwnMessage;
        this.messageId = msg.getMessId();

        setLayout(new BorderLayout(5, 5));

        setOpaque(false);

        setBorder(new EmptyBorder(10, 14, 10, 14));

        setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel senderLabel = new JLabel(msg.getSenderId());
        senderLabel.setFont(senderLabel.getFont().deriveFont(Font.BOLD, 12f));
        senderLabel.setForeground(new Color(210, 210, 210));
        topBar.add(senderLabel, BorderLayout.WEST);

        if (isOwnMessage) {
            JButton optionsBtn = new JButton("...");
            optionsBtn.setForeground(new Color(210, 210, 210));
            optionsBtn.setMargin(new Insets(0, 4, 0, 4));
            optionsBtn.setFocusPainted(false);
            optionsBtn.setContentAreaFilled(false);
            optionsBtn.setBorderPainted(false);
            optionsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            popupMenu.add(editItem);
            popupMenu.add(deleteItem);

            optionsBtn.addActionListener(e -> popupMenu.show(optionsBtn, 0, optionsBtn.getHeight()));
            topBar.add(optionsBtn, BorderLayout.EAST);
        }

        textBody = new JTextArea(msg.getContent()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();

                int dynamicMaxWidth = 900;

                Container viewport = SwingUtilities.getAncestorOfClass(JViewport.class, this);
                if (viewport != null && viewport.getWidth() > 0) {
                    dynamicMaxWidth = Math.max(200, (int) (viewport.getWidth() * 0.75));
                }

                if (d.width < dynamicMaxWidth) {
                    return d;
                }

                javax.swing.text.View view = getUI().getRootView(this);
                view.setSize(dynamicMaxWidth, Integer.MAX_VALUE);
                int wrappedHeight = (int) view.getPreferredSpan(javax.swing.text.View.Y_AXIS);

                return new Dimension(dynamicMaxWidth, wrappedHeight);
            }
        };

        textBody.setEditable(false);
        textBody.setLineWrap(true);
        textBody.setWrapStyleWord(true);

        textBody.setOpaque(false);
        textBody.setForeground(Color.WHITE);
        textBody.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        textBody.setRows(0);

        add(topBar, BorderLayout.NORTH);
        add(textBody, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bubbleColor = isOwnMessage ? new Color(78, 69, 171) : new Color(45, 45, 45);
        g2.setColor(bubbleColor);

        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));

        g2.dispose();
        super.paintComponent(g);
    }

    public void setEditAction(ActionListener actionListener) {
        editItem.addActionListener(actionListener);
    }

    public void setDeleteAction(ActionListener actionListener) {
        deleteItem.addActionListener(actionListener);
    }

    public Long getMessageId() {
        return this.messageId;
    }

    public void updateText(String newText) {
        textBody.setText(newText);
        revalidate();
        repaint();
    }
}