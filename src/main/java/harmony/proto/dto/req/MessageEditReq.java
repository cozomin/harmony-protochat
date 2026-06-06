package harmony.proto.dto.req;

import harmony.proto.dto.BaseDTO;

public class MessageEditReq extends BaseDTO {
    private Long messId;
    private Long chatId;
    private String newContent;

    public MessageEditReq() {
    }

    public MessageEditReq(Long messId, Long chatId, String newContent) {
        this.messId = messId;
        this.chatId = chatId;
        this.newContent = newContent;
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

    public String getNewContent() {
        return newContent;
    }

    public void setNewContent(String newContent) {
        this.newContent = newContent;
    }
}