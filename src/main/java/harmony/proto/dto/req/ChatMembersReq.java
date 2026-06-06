package harmony.proto.dto.req;

import harmony.proto.dto.BaseDTO;

public class ChatMembersReq  extends BaseDTO {
    private Long chatId;

    public ChatMembersReq() {}
    public ChatMembersReq(Long chatId) {
        this.chatId = chatId;
    }

    public Long getChatId() {
        return chatId;
    }
    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }
}
