package harmony.proto.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

public class CoreDTOTest {

    private ObjectMapper mapper;

    @BeforeEach
    public void setup() {
        // Initialize the ObjectMapper before each test
        mapper = new ObjectMapper();

        // For Instant serialization
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testChatDTOSerialization() throws Exception {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        ChatDTO originalDTO = new ChatDTO(10L, "General Chat", true, now);

        String json = mapper.writeValueAsString(originalDTO);

        assertTrue(json.contains("\"type\":\"ChatDTO\""), "JSON string does not contain expected type: ChatDTO");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(ChatDTO.class, deserialized);
        ChatDTO result = (ChatDTO) deserialized;

        assertEquals(10L, result.getChatID());
        assertEquals("General Chat", result.getChatName());
        assertTrue(result.isGroup());
        assertEquals(now, result.getUpdated_at());
    }

    @Test
    public void testMessageDTOSerialization() throws Exception {
        Instant sentTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        MessageDTO originalDTO = new MessageDTO(
                55L,
                "cozo",
                10L,
                "Hello everyone!",
                sentTime,
                "regular"
        );

        String json = mapper.writeValueAsString(originalDTO);

        assertTrue(json.contains("\"type\":\"MessDTO\""), "JSON string does not contain expected type: MessDTO");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(MessageDTO.class, deserialized);
        MessageDTO result = (MessageDTO) deserialized;

        assertEquals(55L, result.getMessId());
        assertEquals("cozo", result.getSenderId());
        assertEquals(10L, result.getChatId());
        assertEquals("Hello everyone!", result.getContent());
        assertEquals(sentTime, result.getSentAt());
        assertEquals("regular", result.getMessageType());
    }

    @Test
    public void testUserDTOSerialization() throws Exception {
        UserDTO originalDTO = new UserDTO("link");

        String json = mapper.writeValueAsString(originalDTO);

        assertTrue(json.contains("\"type\":\"UserDTO\""), "JSON string does not contain expected type: UserDTO");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(UserDTO.class, deserialized);
        UserDTO result = (UserDTO) deserialized;

        assertEquals("link", result.getUsername());
    }
}