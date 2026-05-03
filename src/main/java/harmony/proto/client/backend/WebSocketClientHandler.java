package harmony.proto.client.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import harmony.proto.dto.*;
import harmony.proto.dto.res.ChatRes;
import harmony.proto.dto.res.FriendRes;
import harmony.proto.dto.res.LoginRes;
import harmony.proto.dto.res.MessageRes;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.util.CharsetUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    private volatile boolean authenticated = false;
    private volatile String currentUsername;
    private volatile String loginFailureReason;
    private volatile LiveMessageListener liveMessageListener;

    private volatile CompletableFuture<LoginRes> loginFuture;
    private volatile CompletableFuture<ChatRes> chatFuture;
    private volatile CompletableFuture<MessageRes> messageFuture;
    private volatile CompletableFuture<FriendRes> friendOpFuture;

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public String getLoginFailureReason() {
        return loginFailureReason;
    }

    public void setLiveMessageListener(LiveMessageListener listener) {
        this.liveMessageListener = listener;
    }

    public synchronized void prepareForLogin() {
        authenticated = false;
        currentUsername = null;
        loginFailureReason = null;
        loginFuture = new CompletableFuture<>();
    }

    public synchronized void prepareForChats(){
        chatFuture = new CompletableFuture<>();
    }

    public synchronized void prepareForMessages() {
        messageFuture = new CompletableFuture<>();
    }
    public synchronized void prepareForFriendOp(){ friendOpFuture = new CompletableFuture<>(); }

    public LoginRes awaitLoginResponse() throws Exception {
        return loginFuture.get(5, TimeUnit.SECONDS);
    }

    public ChatRes awaitChatResponse() throws Exception{
        return chatFuture.get(5, TimeUnit.SECONDS);
    }

    public MessageRes awaitMessageResponse() throws Exception{
        return messageFuture.get(5, TimeUnit.SECONDS);
    }

    public FriendRes awaitFriendOpResponse() throws Exception{
        return friendOpFuture.get(5, TimeUnit.SECONDS);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("WebSocket Client disconnected!");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();

        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                System.out.println("WebSocket Client connected!");
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                System.out.println("WebSocket Client failed to connect");
                handshakeFuture.setFailure(e);
            }
            return;
        }

        if (msg instanceof FullHttpResponse response) {
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;

        if (frame instanceof TextWebSocketFrame textFrame) {
            try {

                String json = textFrame.text();
                BaseDTO dto = mapper.readValue(json, BaseDTO.class);

                if (dto instanceof MessageDTO messageDTO){
                    if (liveMessageListener != null) {
                        // Pass the live message out of the handler
                        liveMessageListener.onNewMessage(messageDTO);
                    }
                }
                else if (dto instanceof MessageRes messageRes){
                    messageFuture.complete(messageRes);
                }
                else if (dto instanceof FriendRes friendRes){
                    friendOpFuture.complete(friendRes);
                }
                else if (dto instanceof LoginRes loginRes) {

                    if (loginRes.isSuccess()) {
                        authenticated = true;
                        currentUsername = loginRes.getUsername();
//                        currentUsername = loginRes.getUsername();
                        loginFailureReason = null;
                    } else {
                        authenticated = false;
                        loginFailureReason = loginRes.getMessage();
                    }
                    loginFuture.complete(loginRes);
                }
                else if (dto instanceof ChatRes res){
                    chatFuture.complete(res);
                }

            } catch (Exception e) {
                System.err.println("JSON parsing Error: " + e.getMessage());

                if (loginFuture != null && !loginFuture.isDone()) {
                    loginFuture.completeExceptionally(e);
                }
            }

        } else if (frame instanceof PongWebSocketFrame) {
            System.out.println("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            System.out.println("WebSocket Client received closing");
            ch.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (loginFuture != null && !loginFuture.isDone()) {
            loginFuture.completeExceptionally(cause);
        }
        if (handshakeFuture != null && !handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}