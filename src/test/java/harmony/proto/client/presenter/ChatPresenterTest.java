package harmony.proto.client.presenter;

import harmony.proto.client.backend.WebSocketClient;
import harmony.proto.client.ui.ChatPanel;
import harmony.proto.dto.ChatDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.event.ActionListener;
import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatPresenterTest {

    @Mock private ChatPanel mockChatView;
    @Mock private WebSocketClient mockClient;
    @Mock private InboxPresenter mockInboxPresenter;

    private ChatPresenter presenter;
    private ActionListener sendMessageListener;
    private ActionListener polishListener;

    @BeforeEach
    public void setup() {
        ArgumentCaptor<ActionListener> sendCaptor = ArgumentCaptor.forClass(ActionListener.class);
        ArgumentCaptor<ActionListener> polishCaptor = ArgumentCaptor.forClass(ActionListener.class);

        presenter = new ChatPresenter(mockChatView, mockClient, mockInboxPresenter);

        verify(mockChatView).setSendMessageAction(sendCaptor.capture());
        verify(mockChatView).setPolishAction(polishCaptor.capture());

        sendMessageListener = sendCaptor.getValue();
        polishListener = polishCaptor.getValue();
    }

    @Test
    public void testSendMessage_ValidContent_SendsAndClears() throws Exception {
        ChatDTO mockChat = new ChatDTO();
        mockChat.setChatID(15L);

        Field activeChatField = ChatPresenter.class.getDeclaredField("activeChat");
        activeChatField.setAccessible(true);
        activeChatField.set(presenter, mockChat);

        lenient().when(mockInboxPresenter.getActiveChat()).thenReturn(mockChat);

        lenient().when(mockChatView.getTxtMessage()).thenReturn("Hello Server!");

        sendMessageListener.actionPerformed(null);

        Thread.sleep(400);

        verify(mockClient).sendMessage("Hello Server!", 15L);
        verify(mockChatView).clearTxtMessages();
    }

    @Test
    public void testSendMessage_EmptyContent_Ignored() throws Exception {
        lenient().when(mockChatView.getTxtMessage()).thenReturn("   ");

        sendMessageListener.actionPerformed(null);

        Thread.sleep(400);

        verify(mockClient, never()).sendMessage(anyString(), anyLong());
        verify(mockChatView, never()).clearTxtMessages();
    }

    @Test
    public void testPolishAction_CallsBackend() throws Exception {
        lenient().when(mockChatView.getTxtMessage()).thenReturn("fix dis pls");
        lenient().when(mockClient.polishMessageText("fix dis pls")).thenReturn("Please fix this.");

        polishListener.actionPerformed(null);

        Thread.sleep(400);

        verify(mockClient).polishMessageText("fix dis pls");
    }
}