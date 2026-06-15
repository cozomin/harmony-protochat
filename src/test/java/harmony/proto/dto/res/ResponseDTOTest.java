package harmony.proto.dto.res;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import harmony.proto.dto.BaseDTO;
import harmony.proto.dto.ChatDTO;
import harmony.proto.dto.MessageDTO;
import harmony.proto.dto.UserDTO;
import harmony.proto.dto.req.FriendOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ResponseDTOTest {
    private ObjectMapper mapper;

    @BeforeEach
    public void setup() {
        // Initialize the ObjectMapper before each test
        mapper = new ObjectMapper();
        // For serializing Instant inside ChatDTO
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testAIPolishResSerialization() throws Exception {
        AIPolishRes originalRes = new AIPolishRes("success", "Where is he going?");

        String json = mapper.writeValueAsString(originalRes);
        assertTrue(json.contains("\"type\":\"AIPolishRes\""), "JSON string does not contain expected type: AIPolishRes");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(AIPolishRes.class, deserialized);
        AIPolishRes result = (AIPolishRes) deserialized;

        assertEquals("success", result.getMessage());
        assertEquals("Where is he going?", result.getPolishedText());
    }

    @Test
    public void testAIRecommendGroupsResSerialization() throws Exception {
        List<String> groups = Arrays.asList("Java Devs", "Spring Boot Masters");
        AIRecommendGroupsRes originalRes = new AIRecommendGroupsRes("success", groups);

        String json = mapper.writeValueAsString(originalRes);
        assertTrue(json.contains("\"type\":\"AIRecommendGroupsRes\""), "JSON string does not contain expected type: AIRecommendGroupsRes");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(AIRecommendGroupsRes.class, deserialized);
        AIRecommendGroupsRes result = (AIRecommendGroupsRes) deserialized;

        assertEquals("success", result.getMessage());
        assertIterableEquals(groups, result.getRecommendedGroups());
    }

    @Test
    public void testChatMembersResSerialization() throws Exception {
        List<String> members = Arrays.asList("user1", "user2");
        List<String> topics = Arrays.asList("Tech", "News");
        ChatMembersRes originalRes = new ChatMembersRes(members, topics, "success");

        String json = mapper.writeValueAsString(originalRes);
        assertTrue(json.contains("\"type\":\"ChatMembersRes\""), "JSON string does not contain expected type: ChatMembersRes");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(ChatMembersRes.class, deserialized);
        ChatMembersRes result = (ChatMembersRes) deserialized;

        assertEquals("success", result.getMessage());
        assertIterableEquals(members, result.getChatMembers());
        assertIterableEquals(topics, result.getChatTopics());
    }

    @Test
    public void testChatResSerialization() throws Exception {
        ChatDTO chat1 = new ChatDTO(1L, "General", true, Instant.now());
        ChatDTO chat2 = new ChatDTO(2L, "user2", false, Instant.now());
        List<ChatDTO> chats = Arrays.asList(chat1, chat2);

        ChatRes originalRes = new ChatRes("success", 2L, chats);

        String json = mapper.writeValueAsString(originalRes);
        assertTrue(json.contains("\"type\":\"ChatRes\""), "JSON string does not contain expected type: ChatRes");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(ChatRes.class, deserialized);
        ChatRes result = (ChatRes) deserialized;

        assertEquals("success", result.getMessage());
        assertEquals(2L, result.getNumber());
        assertEquals(2, result.getChats().size());
        assertEquals("General", result.getChats().get(0).getChatName());
    }

    @Test
    public void testFriendResSerialization() throws Exception {
        UserDTO u1 = new UserDTO("cos");
        UserDTO u2 = new UserDTO("link");
        List<UserDTO> users = Arrays.asList(u1, u2);

        FriendRes originalRes = new FriendRes("success", FriendOperation.fetch_accepted, users);

        String json = mapper.writeValueAsString(originalRes);
        assertTrue(json.contains("\"type\":\"FriendRes\""), "JSON string does not contain expected type: FriendRes");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(FriendRes.class, deserialized);
        FriendRes result = (FriendRes) deserialized;

        assertEquals("success", result.getMessage());
        assertEquals(FriendOperation.fetch_accepted, result.getOperation());
        assertEquals(2, result.getUsers().size());
        assertEquals("cos", result.getUsers().get(0).getUsername());
    }

    @Test
    public void testGroupCreationResSerialization() throws Exception {
        GroupCreationRes originalRes = new GroupCreationRes("adminUser", "success");

        String json = mapper.writeValueAsString(originalRes);
        assertTrue(json.contains("\"type\":\"GroupCreationRes\""), "JSON string does not contain expected type: GroupCreationRes");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(GroupCreationRes.class, deserialized);
        GroupCreationRes result = (GroupCreationRes) deserialized;

        assertEquals("success", result.getMessage());
        assertEquals("adminUser", result.getCreator());
    }

    @Test
    public void testInterestsResSerialization() throws Exception {
        List<String> interests = Arrays.asList("Gaming", "Networking");
        InterestsRes originalRes = new InterestsRes("success", interests);

        String json = mapper.writeValueAsString(originalRes);
        assertTrue(json.contains("\"type\":\"InterestsRes\""), "JSON string does not contain expected type: InterestsRes");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(InterestsRes.class, deserialized);
        InterestsRes result = (InterestsRes) deserialized;

        assertEquals("success", result.getMessage());
        assertIterableEquals(interests, result.getInterests());
    }

    @Test
    public void testJoinGroupResSerialization() throws Exception {
        JoinGroupRes originalRes = new JoinGroupRes("success");

        String json = mapper.writeValueAsString(originalRes);
        assertTrue(json.contains("\"type\":\"JoinGroupRes\""), "JSON string does not contain expected type: JoinGroupRes");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(JoinGroupRes.class, deserialized);
        JoinGroupRes result = (JoinGroupRes) deserialized;

        assertEquals("success", result.getMessage());
    }

    @Test
    public void testLeaveGroupResSerialization() throws Exception {
        LeaveGroupRes originalRes = new LeaveGroupRes("success");

        String json = mapper.writeValueAsString(originalRes);
        assertTrue(json.contains("\"type\":\"LeaveGroupRes\""), "JSON string does not contain expected type: LeaveGroupRes");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(LeaveGroupRes.class, deserialized);
        LeaveGroupRes result = (LeaveGroupRes) deserialized;

        assertEquals("success", result.getMessage());
    }

    @Test
    public void testLoginResSerialization() throws Exception {
        LoginRes originalRes = new LoginRes("success", "coolUser");

        String json = mapper.writeValueAsString(originalRes);
        assertTrue(json.contains("\"type\":\"LoginRes\""), "JSON string does not contain expected type: LoginRes");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(LoginRes.class, deserialized);
        LoginRes result = (LoginRes) deserialized;

        assertEquals("success", result.getMessage());
        assertEquals("coolUser", result.getUsername());
        assertTrue(result.isSuccess(), "isSuccess() should return true when message is 'success'");
    }

    @Test
    public void testMessageResSerialization() throws Exception {
        MessageDTO msg1 = new MessageDTO();
        msg1.setMessId(101L);
        msg1.setContent("Hello World");
        msg1.setSentAt(Instant.now());

        List<MessageDTO> messages = List.of(msg1);
        MessageRes originalRes = new MessageRes("success", 1L, messages);

        String json = mapper.writeValueAsString(originalRes);

        assertTrue(json.contains("\"type\":\"MessRes\""), "JSON string does not contain expected type: MessRes");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(MessageRes.class, deserialized);
        MessageRes result = (MessageRes) deserialized;

        assertEquals("success", result.getMessage());
        assertEquals(1L, result.getNumber());
        assertNotNull(result.getChatMessages());
        assertEquals(1, result.getChatMessages().size());

        assertEquals(101L, result.getChatMessages().get(0).getMessId());
        assertEquals("Hello World", result.getChatMessages().get(0).getContent());
    }

    @Test
    public void testMessageUpdateResSerialization() throws Exception {
        MessageUpdateRes originalRes = new MessageUpdateRes(404L, MessageUpdateAction.EDIT, "Corrected text");

        String json = mapper.writeValueAsString(originalRes);
        assertTrue(json.contains("\"type\":\"MessageUpdateRes\""), "JSON string does not contain expected type: MessageUpdateRes");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(MessageUpdateRes.class, deserialized);
        MessageUpdateRes result = (MessageUpdateRes) deserialized;

        assertEquals(404L, result.getMessId());
        assertEquals(MessageUpdateAction.EDIT, result.getAction());
        assertEquals("Corrected text", result.getNewContent());
    }
}