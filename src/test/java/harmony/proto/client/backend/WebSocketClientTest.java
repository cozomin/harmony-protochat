package harmony.proto.client.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import harmony.proto.dto.req.*;
import harmony.proto.dto.res.*;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WebSocketClientTest {

    private WebSocketClient client;
    private Channel mockChannel;
    private WebSocketClientHandler mockHandler;
    private ObjectMapper mapper;

    @BeforeEach
    public void setup() throws Exception {
        client = new WebSocketClient();
        mapper = new ObjectMapper();

        mockChannel = mock(Channel.class);
        mockHandler = mock(WebSocketClientHandler.class);

        // Inject mocked components into the client instance
        injectField(client, "channel", mockChannel);
        injectField(client, "handler", mockHandler);

        when(mockChannel.isActive()).thenReturn(true);
        when(mockHandler.getCurrentUsername()).thenReturn("testUser");
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    public void testLogin_SuccessFlow() throws Exception {
        // Arrange
        LoginRes successRes = new LoginRes("success", "testUser");
        when(mockHandler.awaitLoginResponse()).thenReturn(successRes);

        // Act
        boolean result = client.login("testUser", "password123");

        // Assert
        assertTrue(result, "Login should return true on success");
        verify(mockHandler).prepareForLogin();

        ArgumentCaptor<TextWebSocketFrame> captor = ArgumentCaptor.forClass(TextWebSocketFrame.class);
        verify(mockChannel).writeAndFlush(captor.capture());

        String payload = captor.getValue().text();
        assertTrue(payload.contains("\"username\":\"testUser\""));
        assertTrue(payload.contains("\"password\":\"password123\""));
        assertTrue(payload.contains("\"type\":\"LoginReq\""));
    }

    @Test
    public void testSendMessage_GeneratesCorrectJSON() throws Exception {
        // Act
        client.sendMessage("Hello World", 5L);

        // Assert
        ArgumentCaptor<TextWebSocketFrame> captor = ArgumentCaptor.forClass(TextWebSocketFrame.class);
        verify(mockChannel).writeAndFlush(captor.capture());

        String payload = captor.getValue().text();
        assertTrue(payload.contains("\"content\":\"Hello World\""));
        assertTrue(payload.contains("\"chatId\":5"));
        assertTrue(payload.contains("\"senderId\":\"testUser\""));
        assertTrue(payload.contains("\"type\":\"MessDTO\""));
    }

    @Test
    public void testCreateGroup() throws Exception {
        // Arrange
        List<String> topics = Arrays.asList("Java", "Testing");
        List<String> members = Arrays.asList("cos", "link");
        injectField(client, "username", "testUser");

        // Act
        client.createGroup("Dev Team", topics, members);

        // Assert
        ArgumentCaptor<TextWebSocketFrame> captor = ArgumentCaptor.forClass(TextWebSocketFrame.class);
        verify(mockChannel).writeAndFlush(captor.capture());

        String payload = captor.getValue().text();
        assertTrue(payload.contains("\"name\":\"Dev Team\""));
        assertTrue(payload.contains("\"creator\":\"testUser\""));
        assertTrue(payload.contains("\"type\":\"GroupCreationReq\""));
    }

    @Test
    public void testPolishMessageText() throws Exception {
        // Arrange
        AIPolishRes res = new AIPolishRes("success", "Polished perfectly.");
        when(mockHandler.awaitAIPolishResponse()).thenReturn(res);

        // Act
        String result = client.polishMessageText("Make this good");

        // Assert
        assertEquals("Polished perfectly.", result);
        verify(mockHandler).prepareForAIPolish();

        ArgumentCaptor<TextWebSocketFrame> captor = ArgumentCaptor.forClass(TextWebSocketFrame.class);
        verify(mockChannel).writeAndFlush(captor.capture());
        assertTrue(captor.getValue().text().contains("\"type\":\"AIPolishReq\""));
    }

    @Test
    public void testJoinGroup() throws Exception {
        // Arrange
        JoinGroupRes res = new JoinGroupRes("success");
        when(mockHandler.awaitJoinGroupResponse()).thenReturn(res);

        // Act
        boolean result = client.joinGroup("Global Chat");

        // Assert
        assertTrue(result);
        verify(mockHandler).prepareForJoinGroup();
    }
}