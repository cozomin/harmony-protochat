package harmony.proto.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import harmony.proto.dao.InterestsDao;
import harmony.proto.dao.ChatDao;
import harmony.proto.dao.UserDao;
import harmony.proto.dto.*;
import harmony.proto.dao.MessageDao;
import harmony.proto.database.connection_manager;
import harmony.proto.dto.req.*;
import harmony.proto.dto.res.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.netty.util.AttributeKey;
import org.mindrot.jbcrypt.BCrypt;

import javax.sql.DataSource;
import java.security.MessageDigest;
import java.sql.Connection;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import java.util.Arrays;

//public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    // ChannelGroup holds all active connections so we can broadcast messages
//    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE); // just for 1 group chat
    // 1 Channel = 1 Connection to a Client => ChannelGroup = multiple connections to different clients
//    private static Map<Integer, ChannelGroup> channelGroupMap = new ConcurrentHashMap<>(); // a collection of group chats
    //to be used for multiple groups or something idk

    private static final Map<String, Channel> onlineUsers = new ConcurrentHashMap<String, Channel>();
    private static final Map<Long, List<String>> chatMembers = new ConcurrentHashMap<>();
    public static final AttributeKey<String> usernameKEY = AttributeKey.valueOf("username");

    private static final ExecutorService dbExecutor = Executors.newFixedThreadPool(20);
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private static final ChatLanguageModel aiModel = OllamaChatModel.builder()
            .baseUrl("http://localhost:11434")
            .modelName("qwen2.5:7b") // Needs to be run in terminal beforehand
            .temperature(0.0)
            .build();

    // When someone is added to the server
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
//        // Broadcast to everyone else that someone joined
//        channels.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " has joined!\n"));
//        channels.add(incoming);
//
//        Message systemMsg = new Message();
//        systemMsg.setContent("[SERVER] - " + incoming.remoteAddress() + " joined");
//        systemMsg.setMessageType("SYSTEM");
//
//        try {
//            channels.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(systemMsg)));
//        } catch (Exception e) { e.printStackTrace(); }
        System.out.println("handler added " + incoming.remoteAddress());
    }

    // When someone leaves the server
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        // Broadcast that someone left
//        channels.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " has left!\n"));
//        channels.remove(incoming);
//        Message systemMsg = new Message();
//        systemMsg.setContent("[SERVER] - " + incoming.remoteAddress() + " has left");
//        systemMsg.setMessageType("SYSTEM");
        String sessionUser = ctx.channel().attr(usernameKEY).get();
        if (sessionUser != null) {
            onlineUsers.remove(sessionUser, ctx.channel());
            System.out.println("User " + sessionUser + " has disconnected.");
        }

//        try {
//            channels.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(systemMsg)));
//        } catch (Exception e) { e.printStackTrace(); }
        System.out.println("handler removed " + incoming.remoteAddress());

    }

    // Handles only text right now. It could handle binary messages or other frame types

    /**
     * @see <a href="https://www.w3computing.com/articles/building-high-performance-websocket-applications-java-netty/#:~:text=Advanced%20WebSocket%20Features%3A-,Binary%20and%20text%20messages">More explanations</a>
     */

    //TODO: Handling WebSocket message fragmentation and continuation
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        // Second parameter can be WebSocketFrame and then switch/if elses to verify the data type

        String json = msg.text();
//        System.out.println("SERVER RAW JSON: " + json);

        BaseDTO dto = mapper.readValue(json, BaseDTO.class);
//        System.out.println("SERVER DTO CLASS: " + dto.getClass().getName());

        String sessionUser = ctx.channel().attr(usernameKEY).get();

        if(dto instanceof SignUpReq req){
            CompletableFuture.runAsync(() -> {
                LoginRes res = processSignUpReq(req, ctx);
                String jsonRes = null;
                try {
                    jsonRes = mapper.writeValueAsString(res);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                TextWebSocketFrame frame = new TextWebSocketFrame(jsonRes);
                ctx.channel().writeAndFlush(frame);
            }, dbExecutor);
            return;
        }
        else if (dto instanceof LoginReq login) {
            CompletableFuture.runAsync(() -> {
                LoginRes res = processLoginReq(login, ctx);
                String jsonRes = null;
                try {
                    jsonRes = mapper.writeValueAsString(res);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                TextWebSocketFrame frame = new TextWebSocketFrame(jsonRes);
                ctx.channel().writeAndFlush(frame);
            }, dbExecutor);
            return;
        }

        if (sessionUser == null) {
            System.err.println("Unauthorized request blocked from: " + ctx.channel().remoteAddress());
            //Error DTO here
            return;
        }

        if (dto instanceof MessageDTO messageDTO) {
//            System.out.println("ENTERED MessageDTO branch");
//            System.out.println("sessionUser = " + sessionUser);
//            System.out.println("chatId = " + messageDTO.getChatId());

            String messageText = messageDTO.getContent(); // Client message
            messageDTO.setSenderId(sessionUser);

            if (messageText == null || messageText.isEmpty()) {
                return;
            }

            if (messageDTO.getSentAt() == null) {
                messageDTO.setSentAt(Instant.now());
            }

            //save to database on a different thread
            CompletableFuture.runAsync(() -> {
                saveToDatabase(messageDTO);

                Long chatId = messageDTO.getChatId();

                List<String> participants = chatMembers.computeIfAbsent(chatId, id -> {
                    DataSource ds = connection_manager.getDataSource();
                    ChatDao chatDao = new ChatDao(ds);

                    try {
                        return chatDao.findUsersInChat(id);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return new  ArrayList<>();
                    }
                });

                try {
                    String jsonm = mapper.writeValueAsString(messageDTO);

                    for (String username : participants) {
                        if (username == null) continue;

                        Channel userChannel = onlineUsers.get(username);

                        if (userChannel != null && userChannel.isActive()) {
                            userChannel.writeAndFlush(new TextWebSocketFrame(jsonm));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }, dbExecutor);
        }
        else if (dto instanceof MessageReq req){
            CompletableFuture.runAsync(() -> {
                MessageRes res = processMessageReq(req);
                String jsonRes = null;
                try {
                    jsonRes = mapper.writeValueAsString(res);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                TextWebSocketFrame frame = new TextWebSocketFrame(jsonRes);
                ctx.channel().writeAndFlush(frame);
            }, dbExecutor);
        }
        else if(dto instanceof ChatReq req){
            req.setUsername(sessionUser);
            CompletableFuture.runAsync(() -> {
                ChatRes res = processChatReq(req);
                String jsonRes = null;
                try {
                    jsonRes = mapper.writeValueAsString(res);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                TextWebSocketFrame frame = new TextWebSocketFrame(jsonRes);
                ctx.channel().writeAndFlush(frame);
            }, dbExecutor);
        }
        else if(dto instanceof FriendReq req){
            req.setUser1(sessionUser);
            CompletableFuture.runAsync(() -> {
                FriendRes res = processFriendReq(req);
                String jsonRes = null;
                try {
                    jsonRes = mapper.writeValueAsString(res);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                TextWebSocketFrame frame = new TextWebSocketFrame(jsonRes);
                ctx.channel().writeAndFlush(frame);
            }, dbExecutor);
        }
        else if (dto instanceof GroupCreationReq req) {
            CompletableFuture.runAsync(() -> {
                GroupCreationRes res = processGroupCreationReq(req);
                String jsonRes = null;
                try {
                    jsonRes = mapper.writeValueAsString(res);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                TextWebSocketFrame frame = new TextWebSocketFrame(jsonRes);

                List<String> recipients = new ArrayList<>(req.getMembers());

                if (!recipients.contains(req.getCreator())) {
                    recipients.add(req.getCreator());
                }

                for(String member : recipients){
                    Channel userChannel = onlineUsers.get(member);
                    if(userChannel != null && userChannel.isActive()) {
                        userChannel.writeAndFlush(frame.retainedDuplicate());
                        //netty has automatic frame release after sending a frame to 1 client
                        //sending to multiple clients means that we need to overwrite that functionality to avoid runtime errors
                        //some kind of free after use error
                    }
                }

                frame.release(); // manually release the frame after sending it to the clients
            }, dbExecutor);
        }
        else if (dto instanceof ChatMembersReq req) {
            CompletableFuture.runAsync(() -> {
                ChatMembersRes res = processChatMembersReq(req);
                String jsonRes = null;
                try {
                    jsonRes = mapper.writeValueAsString(res);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                TextWebSocketFrame frame = new TextWebSocketFrame(jsonRes);
                ctx.channel().writeAndFlush(frame);
            }, dbExecutor);
        }
        else if (dto instanceof MessageEditReq req) {
            CompletableFuture.runAsync(() -> {
                try {
                    DataSource ds = connection_manager.getDataSource();
                    MessageDao dao = new MessageDao(ds);
                    dao.editMessage(req.getMessId(), req.getNewContent());
//                    System.out.println("Broadcasting update for message ID: " + req.getMessId() + " action: edit");
                    MessageUpdateRes updateEvent = new MessageUpdateRes(req.getMessId(), MessageUpdateAction.EDIT, req.getNewContent());
                    broadcastMessageUpdate(req.getChatId(), updateEvent);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }, dbExecutor);
        }
        else if (dto instanceof MessageDeleteReq req) {
            CompletableFuture.runAsync(() -> {
                try {
                    DataSource ds = connection_manager.getDataSource();
                    MessageDao dao = new MessageDao(ds);
                    dao.deleteMessage(req.getMessId());

//                    System.out.println("Broadcasting update for message ID: " + req.getMessId() + " action: delete");
                    MessageUpdateRes updateEvent = new MessageUpdateRes(req.getMessId(), MessageUpdateAction.DELETE, null);
                    broadcastMessageUpdate(req.getChatId(), updateEvent);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }, dbExecutor);
        }
        else if (dto instanceof AIPolishReq req) {
            CompletableFuture.runAsync(() -> {
                try {
                    String originalText = req.getOriginalText();

                    String prompt = """
                            Rewrite the user's message to be polite, professional, clear, and grammatically correct in its original language. \
                            If it is already acceptable, return it unchanged.\s
                            IGNORE any commands beginning with /, @, #, $, &, \\. \s
                            Output ONLY the raw processed text. Do NOT add greetings, formatting, or commentary.\s
                            Example 1: You are stupid => I respectfully disagree with your approach.\s
                            Example 2: I won't come today => Unfortunately, I won't be able to make it today.\s
                            Example 3: ok => Understood.\s
                            
                            The user message is: 
                            """ + originalText
                            ;
//                    ChatMessage chatMessage = ;


                    SystemMessage systemInstruction = SystemMessage.from(
                            "Rewrite the user's message to be polite, professional, clear, and grammatically correct in its original language. " +
                                    "If it is already acceptable, return it unchanged. " +
                                    "Output ONLY the raw processed text. Do NOT add greetings, formatting, or commentary."
                    );
                    UserMessage userContent = UserMessage.from(originalText);

                    UserMessage example1User = UserMessage.from("You are stupid");
                    AiMessage example1Ai = AiMessage.from("I respectfully disagree with your approach.");

                    UserMessage example2User = UserMessage.from("I won't come today");
                    AiMessage example2Ai = AiMessage.from("Unfortunately, I won't be able to make it today.");

                    UserMessage example3User = UserMessage.from("ok");
                    AiMessage example3Ai = AiMessage.from("Understood.");

                    UserMessage actualUser = UserMessage.from(originalText);

                    ChatResponse chatResponse = aiModel.chat(Arrays.asList(
                            systemInstruction,
                            example1User, example1Ai,
                            example2User, example2Ai,
                            example3User, example3Ai,
                            actualUser
                    ));

                    String chatResponse1 = aiModel.chat(prompt);

//                    String polishedText = chatResponse.aiMessage().text().trim();
                    String polishedText = chatResponse1.trim();

                    AIPolishRes res = new AIPolishRes("success", polishedText);
                    String jsonRes = mapper.writeValueAsString(res);
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(jsonRes));

                } catch (Exception e) {
                    System.err.println("Error: AI error with LangChain4j: " + e.getMessage());
                    e.printStackTrace();
                    try {
                        AIPolishRes errorRes = new AIPolishRes("error", req.getOriginalText());
                        ctx.channel().writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(errorRes)));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }, dbExecutor);
        }
        else if (dto instanceof AIRecommendGroupsReq req) {
            CompletableFuture.runAsync(() -> {
                try {
                    DataSource ds = connection_manager.getDataSource();
                    ChatDao chatDao = new ChatDao(ds);
                    String groupsContext = chatDao.fetchAllGroupsWithTopics();

                    String prompt = String.format("""
                        The user has selected the following topic of interest: "%s".
                        Here is a list of available chat groups and their associated topics on our server:
                        %s
                        Select 3 to 5 groups from this list that are the most relevant to the user's selected topic.
                        Output ONLY the group names separated by commas. Do not include any other text, greetings, or formatting.
                        If no groups match, or if the list is empty, return an empty string.
                        """, req.getTopic(), groupsContext);

                    String aiResponse = aiModel.chat(prompt);

                    List<String> groups = new ArrayList<>();
                    if (aiResponse != null && !aiResponse.trim().isEmpty()) {
                        for (String g : aiResponse.split(",")) {
                            groups.add(g.trim());
                        }
                    }

                    AIRecommendGroupsRes res = new AIRecommendGroupsRes("success", groups);
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(res)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, dbExecutor);
        }
        else if (dto instanceof InterestsReq req) {
            req.setUsername(sessionUser); // Ensure we use the authenticated user
            CompletableFuture.runAsync(() -> {
                InterestsRes res = processInterestsReq(req);
                try {
                    String jsonRes = mapper.writeValueAsString(res);
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(jsonRes));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }, dbExecutor);
        }
        else if (dto instanceof JoinGroupReq req) {
            CompletableFuture.runAsync(() -> {
                try {
                    DataSource ds = connection_manager.getDataSource();
                    ChatDao dao = new ChatDao(ds);

                    dao.joinGroupByName(req.getGroupName(), sessionUser);

                    JoinGroupRes res = new JoinGroupRes("success");
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(res)));
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        ctx.channel().writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(new JoinGroupRes("error"))));
                    } catch (Exception ex) {}
                }
            }, dbExecutor);
        }
    }

    private void saveToDatabase(MessageDTO msg) {
        DataSource ds = connection_manager.getDataSource();
        MessageDao messageDao = new MessageDao(ds);

        try {
            messageDao.save(msg);
        } catch (Exception e) {
            System.err.println("Failed to save message to DB!");
            e.printStackTrace();
        }
    }

    private ChatRes processChatReq(ChatReq req){
        DataSource ds = connection_manager.getDataSource();
        ChatDao dao = new ChatDao(ds);
        List<ChatDTO> chats;

        try{
            chats = dao.findUserChats(req.getUsername());
        } catch (SQLException e){
            return new ChatRes("Database error" +  e.getMessage(),
                    0L, null);
        }
        return new ChatRes("success", (long) chats.size(), chats);

    }

    public static void clearOnlineUsers() {
        onlineUsers.clear();
    }

    private LoginRes processLoginReq(LoginReq req, ChannelHandlerContext ctx) {

        DataSource ds = connection_manager.getDataSource();
        UserDao userDao = new UserDao(ds);

        try {
            if (!userDao.existsByUsername(req.getUsername())) {
                return new LoginRes("Username does not exist", null);
            }
            String username = userDao.login(req.getUsername(), req.getPassword());
            if(username == null) {
                return new LoginRes("Wrong password!", null);
            }

            Channel oldChannel = onlineUsers.get(username);
            if (oldChannel != null && oldChannel.isActive()) {
                System.out.println("Forcefully disconnecting previous session for: " + username);
                //Error DTO for the other device (or instance) that got kicked out
                oldChannel.close();
            }

            ctx.channel().attr(usernameKEY).set(username);
            onlineUsers.put(username, ctx.channel());
            return new LoginRes("success", username);

        } catch (SQLException e) {
            return new LoginRes("Database failure: " + e.getMessage(), null);
        }
    }

    private String passwordHasher(String password){
        // log_round determines the complexity of the hashing
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    private LoginRes processSignUpReq(LoginReq req, ChannelHandlerContext ctx) {
        DataSource ds = connection_manager.getDataSource();
        UserDao userDao = new UserDao(ds);

        try {
            if (userDao.existsByUsername(req.getUsername())) {
                return new LoginRes("Username already exists!", null);
            }

            String passHashed = passwordHasher(req.getPassword());

            String username = userDao.signUp(req.getUsername(), passHashed);

            ctx.channel().attr(usernameKEY).set(username);
            onlineUsers.put(username, ctx.channel());
            return new LoginRes("success", username);

        } catch (SQLException e) {
            return new LoginRes("Database failure: " + e.getMessage(), null);
        }
    }

    private MessageRes processMessageReq(MessageReq req) {
        DataSource ds = connection_manager.getDataSource();
        MessageDao messageDao = new MessageDao(ds);

        try {
            List<MessageDTO> messages = messageDao.findRecentMessages(req.getChatID(), 200);

            if (messages == null || messages.isEmpty()) {
//                return new MessageRes("no messages", 0L, messages);
                return new MessageRes("success", 0L, messages);
            }

            else return new MessageRes("success", (long) messages.size(), messages);

        } catch (SQLException e) {
            return new MessageRes("Database failure: " + e.getMessage(), null, null);
        }
    }

    private FriendRes processFriendReq(FriendReq req) {
        DataSource ds = connection_manager.getDataSource();
        UserDao dao = new UserDao(ds);
        List<UserDTO> users = new ArrayList<>();
        String user = req.getUser1();

        try {
            switch (req.getOperation()) {
                case FriendOperation.send:
                    if (req.getUser1().equals(req.getUser2()))
                    {
                        return new FriendRes("You can't be friends with yourself :(", null, null);
                    }
                    if (!dao.existsByUsername(req.getUser2())) {
                        return new FriendRes("Username does not exist!", null, null);
                    }
                    if (dao.friendExists(req.getUser1(), req.getUser2()) > 0) {
                        return new FriendRes("Friendship already exists or is pending!", null, null);
                    }
                    dao.sendFriendReq(user, req.getUser2());
                    break;
                case FriendOperation.accept:
                    dao.acceptFriendReq(user, req.getUser2());
                    break;
                case FriendOperation.deny:
                    dao.denyFriendReq(user, req.getUser2());
                    break;
                case FriendOperation.fetch_accepted:
                    users = dao.fetchFriends(user, "accepted");
                    break;
                case FriendOperation.fetch_outgoing:
                    users = dao.fetchFriends(user, "outgoing");
                    break;
                case FriendOperation.fetch_incoming:
                    users = dao.fetchFriends(user, "incoming");
                    break;
            }
        }catch (SQLException e){
            if (e.getMessage().contains("already exists")) {
                return new FriendRes(e.getMessage(), null, null);
            }
            return new FriendRes("DB error! " + e.getMessage(), null, null);
        }
        return new FriendRes("success", req.getOperation(), users);
    }

    private GroupCreationRes processGroupCreationReq(GroupCreationReq req) {
        DataSource ds = connection_manager.getDataSource();
        ChatDao chatDao = new ChatDao(ds);
        try{
            chatDao.addGroup(req.getName(), req.getTopics(), req.getCreator(), req.getMembers());
            return new GroupCreationRes(req.getCreator(), "success");
        }
        catch (SQLException e){
            return new GroupCreationRes(null, "Database failure: " + e.getMessage());
        }
    }

    private ChatMembersRes processChatMembersReq(ChatMembersReq req) {
        DataSource ds = connection_manager.getDataSource();
        ChatDao chatDao = new ChatDao(ds);
        try {
            List<String> chatMembers = chatDao.findUsersInChat(req.getChatId());
            List<String> chatTopics = chatDao.findTopicsForChat(req.getChatId());

            return new ChatMembersRes(chatMembers, chatTopics, "success");
        }
        catch (SQLException e){
            return new ChatMembersRes(null, null, "Database failure: " + e.getMessage());
        }
    }

    private InterestsRes processInterestsReq(InterestsReq req) {
        DataSource ds = connection_manager.getDataSource();
        InterestsDao dao = new InterestsDao(ds);

        try {
            if (InterestsOperation.ADD.name().equals(req.getOperation())) {
                dao.addInterest(req.getUsername(), req.getInterest());
                List<String> updatedInterests = dao.fetchUserInterests(req.getUsername());
                return new InterestsRes("success", updatedInterests);

            } else if (InterestsOperation.FETCH.name().equals(req.getOperation())) {
                List<String> interests = dao.fetchUserInterests(req.getUsername());
                return new InterestsRes("success", interests);

            } else if (InterestsOperation.FETCH_TOP.name().equals(req.getOperation())) {
                // Fetch the top 20 most popular interests across the whole server
                List<String> topInterests = dao.fetchTopInterests(20);
                return new InterestsRes("success", topInterests);

            } else if (InterestsOperation.REMOVE.name().equals(req.getOperation())) {
                dao.removeInterest(req.getUsername(), req.getInterest());
                List<String> updatedInterests = dao.fetchUserInterests(req.getUsername());
                return new InterestsRes("success", updatedInterests);
            }

            return new InterestsRes("Invalid operation", null);
        } catch (SQLException e) {
            return new InterestsRes("Database error: " + e.getMessage(), null);
        }
    }

    private void broadcastMessageUpdate(Long chatId, MessageUpdateRes event) {
        List<String> participants = chatMembers.computeIfAbsent(chatId, id -> {
            DataSource ds = connection_manager.getDataSource();
            ChatDao chatDao = new ChatDao(ds);
            try {
                return chatDao.findUsersInChat(id);
            } catch (SQLException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        });

        if (participants != null && !participants.isEmpty()) {
            try {
                String jsonEvent = mapper.writeValueAsString(event);
                TextWebSocketFrame frame = new TextWebSocketFrame(jsonEvent);
                for (String username : participants) {
                    Channel userChannel = onlineUsers.get(username);
                    if (userChannel != null && userChannel.isActive()) {
                        userChannel.writeAndFlush(frame.retainedDuplicate());
                    }
                }
                frame.release();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // The connection has been idle for 50 seconds
            // Send a PingWebSocketFrame to keep the Cloudflare tunnel alive
            System.out.println("Connection idle, sending Ping...");
            ctx.writeAndFlush(new PingWebSocketFrame());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}