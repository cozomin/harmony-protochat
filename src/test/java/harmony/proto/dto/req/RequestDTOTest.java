package harmony.proto.dto.req;

import com.fasterxml.jackson.databind.ObjectMapper;
import harmony.proto.dto.BaseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RequestDTOTest {
    private ObjectMapper mapper;

    @BeforeEach
    public void setup() {
        // Initialize the ObjectMapper before each test
        mapper = new ObjectMapper();
    }

    @Test
    public void testAIPolishReqSerialization() throws Exception {
        AIPolishReq originalReq = new AIPolishReq("Where is going he?");

        // Serialize
        String json = mapper.writeValueAsString(originalReq);

        // Ensure the type identifier is injected
        assertTrue(json.contains("\"type\":\"AIPolishReq\""), "JSON string does not contain expected type: AIPolishReq");

        // Deserialize through the base class
        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);

        // Ensure it routed to the correct subclass and data matches
        assertInstanceOf(AIPolishReq.class, deserialized);
        AIPolishReq result = (AIPolishReq) deserialized;
        assertEquals("Where is going he?", result.getOriginalText());
    }

    @Test
    public void testAIRecommendGroupsReqSerialization() throws Exception {
        AIRecommendGroupsReq originalReq = new AIRecommendGroupsReq("Java Programming");

        String json = mapper.writeValueAsString(originalReq);
        assertTrue(json.contains("\"type\":\"AIRecommendGroupsReq\""),  "JSON string does not contain expected type: AIRecommendGroupsReq");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(AIRecommendGroupsReq.class, deserialized);
        assertEquals("Java Programming", ((AIRecommendGroupsReq) deserialized).getTopic());
    }

    @Test
    public void testChatMembersReqSerialization() throws Exception {
        ChatMembersReq originalReq = new ChatMembersReq(12345L);

        String json = mapper.writeValueAsString(originalReq);
        assertTrue(json.contains("\"type\":\"ChatMembersReq\""),"JSON string does not contain expected type: ChatMembersReq");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(ChatMembersReq.class, deserialized);
        assertEquals(12345L, ((ChatMembersReq) deserialized).getChatId());
    }

    @Test
    public void testChatReqSerialization() throws Exception {
        ChatReq originalReq = new ChatReq("testUser123");

        String json = mapper.writeValueAsString(originalReq);
        assertTrue(json.contains("\"type\":\"ChatReq\""),"JSON string does not contain expected type: ChatReq");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(ChatReq.class, deserialized);
        assertEquals("testUser123", ((ChatReq) deserialized).getUsername());
    }

    @Test
    public void testFriendReqSerialization() throws Exception {
        FriendReq originalReq = new FriendReq(FriendOperation.send, "user1", "user2");

        String json = mapper.writeValueAsString(originalReq);
        assertTrue(json.contains("\"type\":\"FriendReq\""),"JSON string does not contain expected type: FriendReq");
        assertTrue(json.contains("\"operation\":\"send\""),"JSON string does not contain expected operation: send");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(FriendReq.class, deserialized);
        FriendReq result = (FriendReq) deserialized;
        assertEquals(FriendOperation.send, result.getOperation());
        assertEquals("user1", result.getUser1());
        assertEquals("user2", result.getUser2());
    }

    @Test
    public void testGroupCreationReqSerialization() throws Exception {
        List<String> topics = Arrays.asList("Gaming", "Tech");
        List<String> members = Arrays.asList("user1", "user2");
        GroupCreationReq originalReq = new GroupCreationReq(
                "My Awesome Group",
                topics,
                "A place to hang out",
                members,
                "adminUser"
        );

        String json = mapper.writeValueAsString(originalReq);
        assertTrue(json.contains("\"type\":\"GroupCreationReq\""),"JSON string does not contain expected type: GroupCreationReq");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(GroupCreationReq.class, deserialized);
        GroupCreationReq result = (GroupCreationReq) deserialized;

        assertEquals("My Awesome Group", result.getName());
        assertEquals("A place to hang out", result.getDescription());
        assertEquals("adminUser", result.getCreator());
        assertIterableEquals(topics, result.getTopics());
        assertIterableEquals(members, result.getMembers());
    }

    @Test
    public void testInterestsReqSerialization() throws Exception {
        InterestsReq originalReq = new InterestsReq(InterestsOperation.ADD.name(), "user", "Networking");

        String json = mapper.writeValueAsString(originalReq);
        assertTrue(json.contains("\"type\":\"InterestsReq\""),"JSON string does not contain expected type: InterestsReq");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(InterestsReq.class, deserialized);
        InterestsReq result = (InterestsReq) deserialized;

        assertEquals(InterestsOperation.ADD.name(), result.getOperation());
        assertEquals("user", result.getUsername());
        assertEquals("Networking", result.getInterest());
    }

    @Test
    public void testLoginReqSerialization() throws Exception {
        LoginReq originalReq = new LoginReq("coolUser", "securepa55");

        String json = mapper.writeValueAsString(originalReq);
        assertTrue(json.contains("\"type\":\"LoginReq\""), "JSON string does not contain expected type: LoginReq");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(LoginReq.class, deserialized);
        LoginReq result = (LoginReq) deserialized;

        assertEquals("coolUser", result.getUsername());
        assertEquals("securepa55", result.getPassword());
    }

    @Test
    public void testJoinGroupReqSerialization() throws Exception {
        JoinGroupReq originalReq = new JoinGroupReq("Tech Discussions");

        String json = mapper.writeValueAsString(originalReq);
        assertTrue(json.contains("\"type\":\"JoinGroupReq\""), "JSON string does not contain expected type: JoinGroupReq");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(JoinGroupReq.class, deserialized);
        assertEquals("Tech Discussions", ((JoinGroupReq) deserialized).getGroupName());
    }

    @Test
    public void testMessageDeleteReqSerialization() throws Exception {
        MessageDeleteReq originalReq = new MessageDeleteReq(105L, 42L);

        String json = mapper.writeValueAsString(originalReq);
        assertTrue(json.contains("\"type\":\"MessageDeleteReq\""), "JSON string does not contain expected type: MessageDeleteReq");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(MessageDeleteReq.class, deserialized);
        MessageDeleteReq result = (MessageDeleteReq) deserialized;

        assertEquals(105L, result.getMessId());
        assertEquals(42L, result.getChatId());
    }

    @Test
    public void testMessageEditReqSerialization() throws Exception {
        MessageEditReq originalReq = new MessageEditReq(300L, 12L, "Wait, I meant something else.");

        String json = mapper.writeValueAsString(originalReq);
        assertTrue(json.contains("\"type\":\"MessageEditReq\""), "JSON string does not contain expected type: MessageEditReq");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(MessageEditReq.class, deserialized);
        MessageEditReq result = (MessageEditReq) deserialized;

        assertEquals(300L, result.getMessId());
        assertEquals(12L, result.getChatId());
        assertEquals("Wait, I meant something else.", result.getNewContent());
    }

    @Test
    public void testMessageReqSerialization() throws Exception {
        MessageReq originalReq = new MessageReq(99L);

        String json = mapper.writeValueAsString(originalReq);

        assertTrue(json.contains("\"type\":\"MessReq\""), "JSON string does not contain expected type: MessReq");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(MessageReq.class, deserialized);
        assertEquals(99L, ((MessageReq) deserialized).getChatID());
    }

    @Test
    public void testSignUpReqSerialization() throws Exception {
        SignUpReq originalReq = new SignUpReq("newcomer99", "verySecurepa55!");

        String json = mapper.writeValueAsString(originalReq);
        assertTrue(json.contains("\"type\":\"SignUpReq\""),  "JSON string does not contain expected type: SignUpReq");

        BaseDTO deserialized = mapper.readValue(json, BaseDTO.class);
        assertInstanceOf(SignUpReq.class, deserialized);
        SignUpReq result = (SignUpReq) deserialized;

        assertEquals("newcomer99", result.getUsername());
        assertEquals("verySecurepa55!", result.getPassword());
    }
}
