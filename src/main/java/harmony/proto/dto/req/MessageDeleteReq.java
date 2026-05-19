package harmony.proto.dto.req;

import harmony.proto.dto.BaseDTO;

public class MessageDeleteReq extends BaseDTO {
    private Long messId;
    private Long chatId;

    public MessageDeleteReq() {
    }

    public MessageDeleteReq(Long messId, Long chatId) {
        this.messId = messId;
        this.chatId = chatId;
    }

    public Long getMessId() {
        return messId;
    }

    public void setMessId(Long messId) {
        this.messId = messId;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }
}