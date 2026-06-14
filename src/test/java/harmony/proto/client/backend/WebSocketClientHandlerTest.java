package harmony.proto.client.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import harmony.proto.dto.MessageDTO;
import harmony.proto.dto.res.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WebSocketClientHandlerTest {

    private WebSocketClientHandler handler;
    private EmbeddedChannel channel;
    private WebSocketClientHandshaker mockHandshaker;
    private ObjectMapper mapper;

    @BeforeEach
    public void setup() {
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockHandshaker = mock(WebSocketClientHandshaker.class);
        when(mockHandshaker.isHandshakeComplete()).thenReturn(true);

        handler = new WebSocketClientHandler(mockHandshaker);
        channel = new EmbeddedChannel(handler);
    }

    @Test
    public void testLoginResponse_Success() throws Exception {
        handler.prepareForLogin();
        LoginRes res = new LoginRes("success", "cos");

        channel.writeInbound(new TextWebSocketFrame(mapper.writeValueAsString(res)));

        LoginRes result = handler.awaitLoginResponse();
        assertTrue(result.isSuccess());
        assertEquals("cos", result.getUsername());
        assertTrue(handler.isAuthenticated());
        assertEquals("cos", handler.getCurrentUsername());
    }

    @Test
    public void testLoginResponse_Failure() throws Exception {
        handler.prepareForLogin();
        LoginRes res = new LoginRes("Invalid password", null);

        channel.writeInbound(new TextWebSocketFrame(mapper.writeValueAsString(res)));

        LoginRes result = handler.awaitLoginResponse();
        assertFalse(result.isSuccess());
        assertFalse(handler.isAuthenticated());
        assertEquals("Invalid password", handler.getLoginFailureReason());
    }

    @Test
    public void testChatResponseCompletesFuture() throws Exception {
        handler.prepareForChats();
        ChatRes res = new ChatRes("success", 0L, Collections.emptyList());

        channel.writeInbound(new TextWebSocketFrame(mapper.writeValueAsString(res)));

        ChatRes result = handler.awaitChatResponse();
        assertNotNull(result);
        assertEquals("success", result.getMessage());
    }

    @Test
    public void testLiveMessageListener() throws Exception {
        MessageDTO msg = new MessageDTO("link", 10L, "Hello", null, "regular");
        msg.setMessId(99L);

        LiveMessageListener mockListener = mock(LiveMessageListener.class);
        handler.setLiveMessageListener(mockListener);

        channel.writeInbound(new TextWebSocketFrame(mapper.writeValueAsString(msg)));

        verify(mockListener, times(1)).onNewMessage(argThat(m -> m.getMessId().equals(99L)));
    }

    @Test
    public void testMessageUpdateListener() throws Exception {
        MessageUpdateRes updateRes = new MessageUpdateRes(99L, MessageUpdateAction.EDIT, "New text");

        LiveMessageUpdateListener mockListener = mock(LiveMessageUpdateListener.class);
        handler.setLiveMessageUpdateListener(mockListener);

        channel.writeInbound(new TextWebSocketFrame(mapper.writeValueAsString(updateRes)));

        verify(mockListener, times(1)).onMessageUpdate(argThat(u ->
                u.getMessId().equals(99L) && u.getAction() == MessageUpdateAction.EDIT
        ));
    }

    @Test
    public void testAIPolishResponseCompletesFuture() throws Exception {
        handler.prepareForAIPolish();
        AIPolishRes res = new AIPolishRes("success", "Polished text");

        channel.writeInbound(new TextWebSocketFrame(mapper.writeValueAsString(res)));

        AIPolishRes result = handler.awaitAIPolishResponse();
        assertEquals("Polished text", result.getPolishedText());
    }
}