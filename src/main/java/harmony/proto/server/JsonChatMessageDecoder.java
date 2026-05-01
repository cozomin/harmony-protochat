package harmony.proto.server;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import harmony.proto.dto.MessageDTO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.util.List;

public class JsonChatMessageDecoder extends MessageToMessageDecoder<TextWebSocketFrame> {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    protected void decode(ChannelHandlerContext ctx, TextWebSocketFrame frame, List<Object> out) throws Exception {
        // JSON -> POJO conversion
        MessageDTO msg = mapper.readValue(frame.text(), MessageDTO.class);
        // The object is added to the list to be processed by the next handler
        out.add(msg);
    }
}
